package com.example.javaspeechrecognizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class BertTokenizer {

    private final Map<String, Integer> vocab;
    private static final String CLS_TOKEN = "[CLS]";
    private static final String SEP_TOKEN = "[SEP]";
    private static final String PAD_TOKEN = "[PAD]";
    private static final String UNK_TOKEN = "[UNK]";

    public BertTokenizer(String path) throws IOException {
        vocab = new HashMap<>();
        loadVocab(path);
    }

    // Load the vocabulary into a map (word -> id)
    private void loadVocab(String vocabFilePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(vocabFilePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        int id = 0;
        while ((line = reader.readLine()) != null) {
            vocab.put(line.trim(), id++);
        }
        reader.close();
    }

    // Tokenize input text into subwords and return token IDs
    public List<Integer> tokenize(String text) {
        text = text.toLowerCase();
        List<String> tokens = new ArrayList<>();
        String[] words = text.split("\\s+"); // Split by whitespace

        for (String word : words) {
            List<String> subwords = wordPieceTokenize(word);
            tokens.addAll(subwords);
        }

        List<Integer> tokenIds = new ArrayList<>();
        for (String token : tokens) {
            tokenIds.add(vocab.getOrDefault(token, vocab.get(UNK_TOKEN))); // Default to [UNK] token for unknown words
        }
        return tokenIds;
    }

    // WordPiece tokenization (simplified version)
    // WordPiece tokenization (improved)
    private List<String> wordPieceTokenize(String word) {
        List<String> subwords = new ArrayList<>();

        // If the whole word is in the vocab, add it directly
        if (vocab.containsKey(word)) {
            subwords.add(word);
            return subwords;
        }

        int start = 0;
        while (start < word.length()) {
            int end = word.length();
            String subword = null;

            // Find the longest matching subword
            while (start < end) {
                String candidate = (start > 0 ? "##" : "") + word.substring(start, end);
                if (vocab.containsKey(candidate)) {
                    subword = candidate;
                    break;
                }
                end--;
            }

            // If no subword is found, use [UNK]
            if (subword == null) {
                subwords.add(UNK_TOKEN);
                break;
            }

            subwords.add(subword);
            start = end;
        }

        return subwords;
    }





    // Tokenize input text into subwords and return token IDs
//    public List<Integer> tokenize(String text) {
//        List<String> tokens = new ArrayList<>();
//        String[] words = text.split("\\s+"); // Split by whitespace
//
//        for (String word : words) {
//            List<String> subwords = wordPieceTokenize(word);
//            tokens.addAll(subwords);
//        }
//
//        List<Integer> tokenIds = new ArrayList<>();
//        for (String token : tokens) {
//            tokenIds.add(vocab.getOrDefault(token, vocab.get(UNK_TOKEN))); // Default to [UNK] token for unknown words
//        }
//        return tokenIds;
//    }

    // WordPiece tokenization (simplified version)
//    private List<String> wordPieceTokenize(String word) {
//        // Simple heuristic to split into subwords (you should use a proper BPE tokenizer here for production)
//        List<String> subwords = new ArrayList<>();
//        if (vocab.containsKey(word)) {
//            subwords.add(word); // If the whole word is in the vocab, just add it
//        } else {
//            // Split the word into subwords (e.g., "playing" -> "play", "##ing")
//            subwords.add(word.substring(0, 1)); // First character
//            subwords.add("##" + word.substring(1)); // Remainder as subword
//        }
//        return subwords;
//    }

    // Add special tokens (CLS, SEP)
    public List<Integer> addSpecialTokens(List<Integer> tokenIds) {
        List<Integer> withSpecialTokens = new ArrayList<>();
        withSpecialTokens.add(vocab.get(CLS_TOKEN)); // Add [CLS] at the beginning
        withSpecialTokens.addAll(tokenIds);
        withSpecialTokens.add(vocab.get(SEP_TOKEN)); // Add [SEP] at the end
        return withSpecialTokens;
    }

    // Pad tokens to a fixed length (e.g., 128)
    public List<Integer> padTokens(List<Integer> tokenIds, int maxLength) {
        while (tokenIds.size() < maxLength) {
            tokenIds.add(vocab.get(PAD_TOKEN)); // Pad with [PAD] token
        }
        return tokenIds;
    }

    // Convert token IDs back to tokens
    public String decode(List<Integer> tokenIds) {
        StringBuilder decodedText = new StringBuilder();
        for (int id : tokenIds) {
            for (Map.Entry<String, Integer> entry : vocab.entrySet()) {
                if (entry.getValue() == id) {
                    decodedText.append(entry.getKey()).append(" ");
                    break;
                }
            }
        }
        return decodedText.toString().trim();
    }

    // Convert token IDs back to tokens
    public List<String> decodeToStrings(List<Integer> tokenIds) {
        List<String> decodedTokens = new ArrayList<>();
        for (int id : tokenIds) {
            for (Map.Entry<String, Integer> entry : vocab.entrySet()) {
                if (entry.getValue() == id) {
                    decodedTokens.add(entry.getKey());
                    break;
                }
            }
        }
        return decodedTokens;
    }
}
