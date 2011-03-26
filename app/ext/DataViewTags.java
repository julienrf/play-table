package ext;

import groovy.lang.Closure;
import play.exceptions.TagInternalException;
import play.exceptions.TemplateExecutionException;
import play.templates.FastTags;
import play.templates.GroovyTemplate.ExecutableTemplate;
import play.templates.TagContext;

import java.io.PrintWriter;
import java.util.Map;

public class DataViewTags extends FastTags {

    public static void _table(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        Iterable<?> data = (Iterable<?>) args.get("arg");
        if (data == null) {
            throw new TemplateExecutionException(template.template, fromLine, "Please specify the data to display", new TagInternalException("Please specifiy the data to display"));
        }
        String it = (String) args.get("as");
        if (it == null) {
            throw new TemplateExecutionException(template.template, fromLine, "Missing parameter 'as'", new TagInternalException("Missing parameter 'as'"));
        }

        if (!args.containsKey("class")) {
            out.println("<table>");
        } else {
            out.println("<table class=\"" + args.get("class") + "\">");
        }

        String rowClass = args.containsKey("rowClass") ? (String) args.get("rowClass") : "";

        if (body == null) {
            // Automatically fill the table content with the models properties
        } else {
            // Fill the table content with the execution of its body

            // A first time for the header row
            TagContext.current().data.put("dataview.state", "head");
            if (rowClass.isEmpty()) {
                out.println("<tr>");
            } else {
                out.println("<tr class=\"" + rowClass + "\">");
            }
            body.call();
            out.println("</tr>");

            // Then for each row
            TagContext.current().data.put("dataview.state", "content");
            int index = 0;
            for (Object row : data) {
                String parity = (index % 2) == 0 ? " even" : " odd";
                out.println("<tr class=\"" + rowClass + parity + "\">");

                body.setProperty(it, row);
                body.call();

                out.println("</tr>");

                index++;
            }
        }
        out.println("</table>");
    }

    public static void _column(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        String state = (String) TagContext.parent("table").data.get("dataview.state");
        if (state.equals("head")) {
            out.print("<th>");
            if (args.containsKey("arg")) {
                out.print(args.get("arg"));
            }
            out.println("</th>");
        } else {
            if (!args.containsKey("class")) {
                out.print("<td>");
            } else {
                out.print("<td class=\"" + args.get("class") + "\">");
            }
            if (body != null) {
                body.call();
            }
            out.println("</td>");
        }
    }
}
