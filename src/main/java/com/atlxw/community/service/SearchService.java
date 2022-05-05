package com.atlxw.community.service;

import java.util.List;
import java.util.Map;

public interface SearchService {
    Map<String, List<Map<String, String>>> searchPreview(String q);
}
