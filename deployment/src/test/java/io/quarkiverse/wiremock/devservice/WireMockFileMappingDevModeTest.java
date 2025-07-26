package io.quarkiverse.wiremock.devservice;

import static io.quarkiverse.wiremock.devservice.ConfigProviderResource.BASE_URL;
import static io.quarkiverse.wiremock.devservice.WireMockConfigKey.PORT;
import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.OK;

import java.util.function.Consumer;

import io.restassured.http.ContentType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.wiremock.items.WireMockFileMappingBuildItem;
import io.quarkus.builder.BuildChainBuilder;
import io.quarkus.builder.BuildContext;
import io.quarkus.builder.BuildStep;
import io.quarkus.builder.BuildStepBuilder;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public class WireMockFileMappingDevModeTest {
    private static final String APP_PROPERTIES = "application-builditems.properties";

    @RegisterExtension
    static final QuarkusUnitTest DEV_MODE_TEST = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class).addClass(ConfigProviderResource.class)
                            .addAsResource(APP_PROPERTIES, "application.properties"))
            .addBuildChainCustomizer(new Consumer<BuildChainBuilder>() {
                @Override
                public void accept(BuildChainBuilder buildChainBuilder) {
                    BuildStepBuilder stepBuilder = buildChainBuilder.addBuildStep(new BuildStep() {
                        @Override
                        public void execute(BuildContext context) {
                            context.produce(new WireMockFileMappingBuildItem(
                                    "products.json", """
                                            {
                                              "request": {
                                                "method": "GET",
                                                "urlPath": "/products",
                                                "headers": {
                                                  "Accept": {
                                                    "matches": "application/json"
                                                  }
                                                }
                                              },
                                              "response": {
                                                "status": 200,
                                                "body": "[]",
                                                "headers": {
                                                  "Content-Type": "application/json"
                                                }
                                              }
                                            }
                                            """));

                        }
                    });
                    stepBuilder.produces(WireMockFileMappingBuildItem.class).build();
                }
            });

    @Test
    void testFileMappingGeneratedByBuildItem() {
        String port = RestAssured.get(format("%s/config?propertyName=%s", BASE_URL, PORT)).then().extract().asString();
        RestAssured
                .given()
                .accept("application/json")
                .when()
                .get(format("http://localhost:%s/products", port))
                .then().statusCode(OK).log();
    }

}
