package com.targaryen.marketintel.service;

import com.targaryen.marketintel.model.DiffResult;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class DiffService {

    public DiffResult extractDiff(String oldText, String newText) {
        if (oldText == null) {
            return new DiffResult(newText != null ? newText : "");
        }
        if (newText == null) {
            return new DiffResult("");
        }

        // Basic string chunking by lines/paragraphs
        Set<String> oldLines = new HashSet<>(Arrays.asList(oldText.split("\\r?\\n")));
        String[] newLines = newText.split("\\r?\\n");

        StringBuilder diffBuilder = new StringBuilder();
        for (String line : newLines) {
            line = line.trim();
            // If the line is new or modified and not empty, we extract it.
            if (!line.isEmpty() && !oldLines.contains(line)) {
                diffBuilder.append(line).append("\n");
            }
        }

        return new DiffResult(diffBuilder.toString().trim());
    }
}
