package rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.containers.errors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.paour.github.natorder.NaturalOrderComparator;

/**
 * This is a processed error message object consisting of a message and its parameters. Since Port
 * Authority refuses to make errors work as expected,
 *
 * @author Jeremy Jao
 * @since 49
 */
public class ErrorMessage {

    /**
     * The message of the error.
     */
    private String message;

    /**
     * The combined parameters for the message.
     */
    private String parameters;


    /**
     * Creates a processed error message.
     * @param message the message for the error
     * @param parameterList the parameters that make the error.
     */
    public ErrorMessage(String message, List<String> parameterList) {
        this.message = message;
        this.parameters = commaDelimitParams(parameterList);
    }

    /**
     * Gets the error message.
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the parameters in String form.
     * @return a list of parameters for the message.
     */
    public String getParameters() {
        return parameters;
    }

    /**
     * Makes a comma-delimited String from a list.
     * @param parameters The iterable parameters.
     * @return the parameters in string form.
     */
    private String commaDelimitParams(List<String> parameters) {
        if(parameters == null || parameters.isEmpty() || parameters.get(0) == null) return null;
        parameters = new ArrayList<>(parameters);
        Collections.sort(parameters, new NaturalOrderComparator<>());
        StringBuilder paramBuffer = new StringBuilder();
        boolean isFirst = true;
        for (String param : parameters) {
            if(isFirst) {
                isFirst = false;
            } else
                paramBuffer.append(", ");
            paramBuffer.append(param);
        }
        return paramBuffer.toString();
    }
}
