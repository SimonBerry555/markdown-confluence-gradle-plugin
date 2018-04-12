package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.github.qwazer.markdown.confluence.core.ConfluenceConfig.DEFAULT_PARSE_TIMEOUT;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@Service
public class MainService {

    private final FileReaderService fileReaderService;
    private MarkdownService markdownService;
    private final PageService pageService;

    @Autowired
    public MainService(FileReaderService fileReaderService,
                       MarkdownService markdownService,
                       PageService pageService) {
        this.fileReaderService = fileReaderService;
        this.markdownService = markdownService;
        this.pageService = pageService;
    }

    public void processAll(ConfluenceConfig confluenceConfig) throws IOException {

        if (confluenceConfig.getParseTimeout() != DEFAULT_PARSE_TIMEOUT) {
            this.markdownService = new MarkdownService(confluenceConfig.getParseTimeout());
        }

        List<Page> orderedList = order(confluenceConfig.getPages());
        for (Page page : orderedList) {
            String plainFileContent = fileReaderService.readFile(page);
            String wikiText = markdownService.convertMarkdown2Wiki(plainFileContent, confluenceConfig);
            pageService.postWikiPageToConfluence(page, confluenceConfig, wikiText);
        }
    }


    public static List<Page> order(Collection<Page> pages){
        if (pages.isEmpty()) return Collections.EMPTY_LIST;

        HashMap<Integer,Collection<Page>> group = new HashMap<>();

        Collection<Page> roots = new ArrayList<>();
        Collection<Page> childs = new ArrayList<>();
        for (Page page : pages){
            if (hasParent(page, pages)) {
               childs.add(page);
            } else {
               roots.add(page);
            }
        }

        LinkedList<Page> linkedList = new LinkedList<>();

        linkedList.addAll(roots);
        linkedList.addAll(order(childs));
        return linkedList;

    }

    private static boolean hasParent(Page page, Collection<Page> pages) {
        boolean res = false;
        for (Page curr  : pages){
            if (curr.getTitle().equals(page.getParentTitle())){
                return true;
            }
        }

        return res;
    }





}
