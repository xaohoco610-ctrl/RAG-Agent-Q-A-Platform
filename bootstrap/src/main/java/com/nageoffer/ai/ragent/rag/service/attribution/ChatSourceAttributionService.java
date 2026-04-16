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

package com.nageoffer.ai.ragent.rag.service.attribution;

import com.nageoffer.ai.ragent.framework.convention.RetrievedChunk;

import java.util.List;
import java.util.Map;

/**
 * 对话来源归因服务，负责将检索结果聚合为可展示的文档来源信息。
 */
public interface ChatSourceAttributionService {

    /**
     * 根据本轮检索命中的 chunk，生成文档级来源尾注文本。
     *
     * @param intentChunks 意图 -> 命中分片
     * @param topK         本轮最终使用的 topK
     * @return 尾注文本；无有效来源时返回空串
     */
    String buildDocumentSourceAppendix(Map<String, List<RetrievedChunk>> intentChunks, int topK);
}

