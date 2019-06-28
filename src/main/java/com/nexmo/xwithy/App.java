package com.nexmo.xwithy;

import com.nexmo.client.incoming.InputEvent;
import com.nexmo.client.voice.ncco.InputAction;
import com.nexmo.client.voice.ncco.Ncco;
import com.nexmo.client.voice.ncco.TalkAction;
import spark.Request;

import static spark.Spark.port;
import static spark.Spark.post;

/**
 * A simple Spark Framework microservice which handles inbound Nexmo calls.
 */
public class App {
    public static void main(String[] args) {
        // Host on port 4567:
        port(4567);

        /*
         * Post requests to /inbound indicate an incoming call.
         */
        post("/inbound", (req, res) -> {
            // Return an NCCO, with 2 actions. First ask them to enter a digit,
            // then wait for the entered digit, which will be sent to the
            // '/input' endpoint:
            res.type("application/json");
            return new Ncco(
                    TalkAction.builder("Welcome to my Nexmo IVR! Please enter a digit.")
                            .build(),
                    InputAction.builder()
                            .maxDigits(1)
                            .timeOut(5)
                            .eventUrl(pathToUrl(req, "/input"))
                            .build()
            ).toJson();
        });

        /*
         * Post requests to /input indicate a DTMF input webhook call.
         */
        post("/input", (req, res) -> {
            // Parse the received JSON body into an InputEvent object:
            InputEvent input = InputEvent.fromJson(req.body());

            // Return an NCCO to the caller, telling them the DTMF digit
            // they provided:
            res.type("application/json");
            return new Ncco(
                    TalkAction.builder("You entered " + input.getDtmf())
                            .build()
            ).toJson();
        });
    }

    /**
     * A utility method to generate a URL from a received request (used to obtain the host and whether the request is
     * HTTP or HTTPS) and a path.
     *
     * @param req  The request to retrieve header and URL information from.
     * @param path The path to append to the URL
     *
     * @return The generated URL with the path appended.
     */
    private static String pathToUrl(Request req, String path) {
        // Ngrok passes us this header:
        String protocol = req.headers("X-Forwarded-Proto");
        if (protocol == null) {
            // If the request hasn't been forwarded by a proxy,
            // just use the scheme being used with Jetty:
            protocol = req.scheme();
        }

        return protocol + "://" + req.host() + path;
    }
}
