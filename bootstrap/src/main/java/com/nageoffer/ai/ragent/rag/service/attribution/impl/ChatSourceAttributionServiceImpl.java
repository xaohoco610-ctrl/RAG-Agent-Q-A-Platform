/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nageoffer.ai.ragent.rag.service.attribution.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.ai.ragent.framework.convention.RetrievedChunk;
import com.nageoffer.ai.ragent.knowledge.dao.entity.KnowledgeBaseDO;
import com.nageoffer.ai.ragent.knowledge.dao.entity.KnowledgeChunkDO;
import com.nageoffer.ai.ragent.knowledge.dao.entity.KnowledgeDocumentDO;
import com.nageoffer.ai.ragent.knowledge.dao.mapper.KnowledgeBaseMapper;
import com.nageoffer.ai.ragent.knowledge.dao.mapper.KnowledgeChunkMapper;
import com.nageoffer.ai.ragent.knowledge.dao.mapper.KnowledgeDocumentMapper;
import com.nageoffer.ai.ragent.rag.service.attribution.ChatSourceAttributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatSourceAttributionServiceImpl implements ChatSourceAttributionService {

    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public String buildDocumentSourceAppendix(Map<String, List<RetrievedChunk>> intentChunks, int topK) {
        List<RetrievedChunk> topChunks = resolveTopChunks(intentChunks, topK);
        if (topChunks.isEmpty()) {
            return "";
        }

        List<String> chunkIds = topChunks.stream()
                .map(RetrievedChunk::getId)
                .filter(StrUtil::isNotBlank)
                .toList();
        if (chunkIds.isEmpty()) {
            return "";
        }

        List<KnowledgeChunkDO> chunkRecords = knowledgeChunkMapper.selectList(
                Wrappers.lambdaQuery(KnowledgeChunkDO.class)
                        .in(KnowledgeChunkDO::getId, chunkIds)
                        .eq(KnowledgeChunkDO::getDeleted, 0)
        );
        if (CollUtil.isEmpty(chunkRecords)) {
            return "";
        }

        Map<String, KnowledgeChunkDO> chunkById = chunkRecords.stream()
                .collect(Collectors.toMap(KnowledgeChunkDO::getId, it -> it, (a, b) -> a));

        Set<String> docIds = chunkRecords.stream()
                .map(KnowledgeChunkDO::getDocId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Set<String> kbIds = chunkRecords.stream()
                .map(KnowledgeChunkDO::getKbId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        if (docIds.isEmpty() || kbIds.isEmpty()) {
            return "";
        }

        Map<String, KnowledgeDocumentDO> docById = knowledgeDocumentMapper.selectList(
                        Wrappers.lambdaQuery(KnowledgeDocumentDO.class)
                                .in(KnowledgeDocumentDO::getId, docIds)
                                .eq(KnowledgeDocumentDO::getDeleted, 0)
                ).stream()
                .collect(Collectors.toMap(KnowledgeDocumentDO::getId, it -> it, (a, b) -> a));
        Map<String, KnowledgeBaseDO> kbById = knowledgeBaseMapper.selectList(
                        Wrappers.lambdaQuery(KnowledgeBaseDO.class)
                                .in(KnowledgeBaseDO::getId, kbIds)
                                .eq(KnowledgeBaseDO::getDeleted, 0)
                ).stream()
                .collect(Collectors.toMap(KnowledgeBaseDO::getId, it -> it, (a, b) -> a));

        LinkedHashSet<String> orderedDocKeys = new LinkedHashSet<>();
        for (RetrievedChunk chunk : topChunks) {
            KnowledgeChunkDO chunkDO = chunkById.get(chunk.getId());
            if (chunkDO == null || StrUtil.hasBlank(chunkDO.getKbId(), chunkDO.getDocId())) {
                continue;
            }
            orderedDocKeys.add(buildDocKey(chunkDO.getKbId(), chunkDO.getDocId()));
        }

        if (orderedDocKeys.isEmpty()) {
            return "";
        }

        List<String> lines = new ArrayList<>();
        int index = 1;
        for (String key : orderedDocKeys) {
            String[] parts = key.split("::", 2);
            if (parts.length != 2) {
                continue;
            }
            KnowledgeBaseDO kb = kbById.get(parts[0]);
            KnowledgeDocumentDO doc = docById.get(parts[1]);
            if (kb == null || doc == null) {
                continue;
            }
            String kbName = StrUtil.blankToDefault(kb.getName(), kb.getId());
            String docName = StrUtil.blankToDefault(doc.getDocName(), doc.getId());
            lines.add(index + ". " + kbName + " / " + docName);
            index++;
        }

        if (lines.isEmpty()) {
            return "";
        }

        return "\n\n参考文档：\n" + String.join("\n", lines);
    }

    private List<RetrievedChunk> resolveTopChunks(Map<String, List<RetrievedChunk>> intentChunks, int topK) {
        if (intentChunks == null || intentChunks.isEmpty()) {
            return List.of();
        }

        Map<String, RetrievedChunk> deduplicated = new LinkedHashMap<>();
        for (List<RetrievedChunk> chunkList : intentChunks.values()) {
            if (CollUtil.isEmpty(chunkList)) {
                continue;
            }
            for (RetrievedChunk chunk : chunkList) {
                if (chunk == null || StrUtil.isBlank(chunk.getId())) {
                    continue;
                }
                RetrievedChunk existing = deduplicated.get(chunk.getId());
                if (existing == null || compareScore(chunk, existing) < 0) {
                    deduplicated.put(chunk.getId(), chunk);
                }
            }
        }

        int limit = topK > 0 ? topK : Integer.MAX_VALUE;
        return deduplicated.values().stream()
                .sorted(this::compareScore)
                .limit(limit)
                .toList();
    }

    /**
     * 分数降序：高分优先，null 分数排最后。
     */
    private int compareScore(RetrievedChunk left, RetrievedChunk right) {
        Float ls = left == null ? null : left.getScore();
        Float rs = right == null ? null : right.getScore();
        return Comparator.nullsLast(Comparator.<Float>reverseOrder())
                .compare(ls, rs);
    }

    private String buildDocKey(String kbId, String docId) {
        return Objects.toString(kbId, "") + "::" + Objects.toString(docId, "");
    }
}

