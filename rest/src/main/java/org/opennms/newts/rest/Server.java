package org.opennms.newts.rest;


import static spark.Spark.get;
import static spark.Spark.post;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.type.TypeReference;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.ValueType;

import spark.Request;
import spark.Response;
import spark.Route;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.inject.Guice;
import com.google.inject.Injector;


public class Server {

    private static final String ALLOW_CORS = "*";

    private Function<Row<Sample>, Collection<SampleDTO>> m_rowFunc = new Function<Row<Sample>, Collection<SampleDTO>>() {

        @Override
        public Collection<SampleDTO> apply(Row<Sample> input) {
            return Collections2.transform(input.getElements(), m_toSampletDTO);
        }
    };

    private Function<Sample, SampleDTO> m_toSampletDTO = new Function<Sample, SampleDTO>() {

        @Override
        public SampleDTO apply(Sample input) {
            SampleDTO output = new SampleDTO();
            output.setResource(input.getResource());
            output.setTimestamp(input.getTimestamp().asMillis());
            output.setValue(input.getValue());
            output.setName(input.getName());
            output.setType(input.getType());
            return output;
        }
    };

    private Function<SampleDTO, Sample> m_fromSampleDTO = new Function<SampleDTO, Sample>() {

        @Override
        public Sample apply(SampleDTO m) {
            return new Sample(
                    new Timestamp(m.getTimestamp(), TimeUnit.MILLISECONDS),
                    m.getResource(),
                    m.getName(),
                    m.getType(),
                    ValueType.compose(m.getValue(), m.getType()));
        }
    };

    private final SampleRepository m_repository;

    @Inject
    public Server(final SampleRepository repository) {
        m_repository = repository;
        initialize();
    }

    private void initialize() {

        post(new Route("/") {

            @Override
            public Object handle(Request request, Response response) {

                ObjectMapper mapper = new ObjectMapper();
                ObjectReader reader = mapper.reader(new TypeReference<List<SampleDTO>>() {
                });
                Collection<SampleDTO> sampleDTOs = null;

                try {
                    sampleDTOs = reader.readValue(request.body());
                }
                catch (IOException e) {
                    halt(400, String.format("Unable to parse request body as JSON (reason: %s) ", e.getMessage()));
                }

                m_repository.insert(Collections2.transform(sampleDTOs, m_fromSampleDTO));

                return "";
            }
        });

        get(new JsonTransformerRoute<Object>("/:resource") {

            @Override
            public Object handle(Request request, Response response) {

                String resource = request.params(":resource");

                Results<Sample> select = m_repository.select(resource, getStart(request), getEnd(request));

                response.header("Access-Control-Allow-Origin", ALLOW_CORS); // Allow CORS
                response.type("application/json");

                return Collections2.transform(select.getRows(), m_rowFunc);
            }
        });

    }

    // FIXME: These methods can raise NumberFormatExceptions if query params aren't numbers
    private Optional<Timestamp> getStart(Request request) {
        String param = request.queryParams("start");
        if (param == null) return Optional.of(Timestamp.now().minus(Duration.seconds(86400)));
        return Optional.of(new Timestamp(Integer.parseInt(param), TimeUnit.SECONDS));
    }

    private Optional<Timestamp> getEnd(Request request) {
        String param = request.queryParams("end");
        if (param == null) return Optional.of(Timestamp.now());
        return Optional.of(new Timestamp(Integer.parseInt(param), TimeUnit.SECONDS));
    }

    public static void main(String... args) {

        Injector injector = Guice.createInjector(new Config());
        injector.getInstance(Server.class);

    }

}
