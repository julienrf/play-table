package tags;

import groovy.lang.Closure;
import play.Play;
import play.db.Model;
import play.exceptions.TagInternalException;
import play.exceptions.TemplateExecutionException;
import play.i18n.Messages;
import play.templates.FastTags;
import play.templates.GroovyTemplate.ExecutableTemplate;
import play.templates.TagContext;

import java.io.PrintWriter;
import java.rmi.UnexpectedException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public class Table extends FastTags {

    public static void _table(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {

        // Retrieve the data
        final Iterable<?> data = (Iterable<?>) args.get("arg");
        if (data == null) {
            throw new TemplateExecutionException(template.template,
                                                 fromLine,
                                                 "Please specify the data to display",
                                                 new TagInternalException("Please specifiy the data to display"));
        }

        // Ensure data are not empty
        if (!data.iterator().hasNext()) {
            out.println(Messages.get("table.nodata"));
            return;
        }

        TableContentPrinter contentPrinter;

        if (body == null) {
            // Automatically fill the table content with the models properties
            final Iterable<? extends Model> modelData = (Iterable<? extends Model>)data;
            final Class<? extends Model> clazz = modelData.iterator().next().getClass();
            if (Model.class.isAssignableFrom(clazz)) {
                Map<String, String> properties;
                if (args.containsKey("columns")) {
                    properties = (Map<String, String>)args.get("columns");
                } else {
                    // Display all properties
                    properties = new LinkedHashMap<String, String>();
                    for(Model.Property property : Model.Manager.factoryFor(clazz).listProperties()) {
                        properties.put(property.name, Messages.get(property.name));
                    }
                }
                contentPrinter = new ModelPrinter(properties);
            } else {
                throw new TemplateExecutionException(template.template,
                                                     fromLine,
                                                     "Please use Play! models in the 'table' tag",
                                                     new TagInternalException("Please use Play! models in the 'table' tag"));
            }
        } else {
            // Fill the table content with the execution of its body
            final String it = (String) args.get("as");
            if (it == null) {
                throw new TemplateExecutionException(template.template,
                                                     fromLine,
                                                     "Missing parameter 'as'",
                                                     new TagInternalException("Missing parameter 'as'"));
            }
            contentPrinter = new TagPrinter(body, it);
        }
        
        // Handle the optional “class” parameter
        if (!args.containsKey("class")) {
            out.println("<table>");
        } else {
            out.println("<table class=\"" + args.get("class") + "\">");
        }

        // Handle the optional “rowClass” parameter
        final String rowClass = args.containsKey("rowClass") ? (String) args.get("rowClass") : "";

        // A first time for the header row
        printStartTr(rowClass, out);
        contentPrinter.printHead(out);
        printEndTr(out);

        // Then for each row
        int index = 1;
        for (Object row : data) {
            String parity = (index % 2) == 0 ? " even" : " odd";

            printStartTr(rowClass + parity, out);

            contentPrinter.printRow(row, out);

            printEndTr(out);

            index++;
        }
        out.println("</table>");
    }

    public static void _column(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        // Retrieve the table state (“head” or “content”)
        final String state = (String) TagContext.parent("table").data.get("dataview.state");
        if (state == null) {
            throw new TemplateExecutionException(template.template,
                                                 fromLine,
                                                 "The 'column' tag should be used inside a 'table' tag",
                                                 new TagInternalException("The 'column' tag should be used inside a 'table' tag"));
        }
        if (state.equals("head")) {
            // Display the label
            out.print("<th>");
            if (args.containsKey("arg")) {
                out.print(args.get("arg"));
            }
            out.println("</th>");
        } else {
            // Display the content
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

    private static void printStartTr(final String rowClass, final PrintWriter out) {
        if (rowClass.isEmpty()) {
            out.println("<tr>");
        } else {
            out.println("<tr class=\"" + rowClass + "\">");
        }
    }

    private static void printEndTr(final PrintWriter out) {
        out.println("</tr>");
    }

    private interface TableContentPrinter {
        void printHead(final PrintWriter out);
        void printRow(final Object data, final PrintWriter out);
    }

    private static class TagPrinter implements TableContentPrinter {
        private Closure body;
        private String it;

        public TagPrinter(final Closure body, final String it) {
            this.it = it;
            this.body = body;
        }

        @Override
        public void printHead(final PrintWriter out) {
            TagContext.current().data.put("dataview.state", "head");
            body.call();
            TagContext.current().data.put("dataview.state", "content");
        }

        @Override
        public void printRow(final Object data, final PrintWriter out) {
            // Provide a variable to the body and call it
            body.setProperty(it, data);
            body.call();
        }
    }

    private static class ModelPrinter implements TableContentPrinter {
        private Map<String, String> properties;

        public ModelPrinter(final Map<String, String> properties) {
            this.properties = properties;
        }

        @Override
        public void printHead(final PrintWriter out) {
            for (String label : properties.values()) {
                out.print("<th>");
                out.print(label);
                out.println("</th>");
            }
        }

        @Override
        public void printRow(final Object data, final PrintWriter out) {
            Model model = (Model)data;
            for (String property : properties.keySet()) {
                out.print("<td>");
                try {
                    out.print(model.getClass().getField(property).get(model));
                } catch (Throwable t) {}
                out.println("</td>");
            }
        }
    }
}
