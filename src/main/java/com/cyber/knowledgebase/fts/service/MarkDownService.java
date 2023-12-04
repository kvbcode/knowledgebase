package com.cyber.knowledgebase.fts.service;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class MarkDownService {
    private final String htmlTemplate;

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkDownService() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options)
                .indentSize(4)
                .build();
        htmlTemplate = readResourceAsString("markdown_template.html");
    }

    private String readResourceAsString(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try {
            return resource.getContentAsString(UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String mapToHtmlBody(String markdownBody) {
        Node document = parser.parse(markdownBody);
        return renderer.render(document);
    }

    public String mapToHtmlDocument(String markdownBody, String title) {
        return htmlTemplate.formatted(title, mapToHtmlBody(markdownBody));
    }

    public Resource mapToHtmlResource(Resource markdownResource) {
        String markdownBody;
        try {
            markdownBody = markdownResource.getContentAsString(UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String htmlDocument = mapToHtmlDocument(markdownBody, markdownResource.getFilename());
        return new ByteArrayResource(htmlDocument.getBytes(UTF_8));
    }

}
