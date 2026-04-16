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

package com.nageoffer.ai.ragent.rag.service.handler;

import cn.hutool.core.util.StrUtil;
import com.nageoffer.ai.ragent.infra.chat.StreamCallback;
import lombok.RequiredArgsConstructor;

/**
 * 在流式输出完成前追加来源尾注的回调装饰器。
 */
@RequiredArgsConstructor
public class SourceAppendingStreamCallback implements StreamCallback {

    private final StreamCallback delegate;
    private final String sourceAppendix;

    @Override
    public void onContent(String chunk) {
        delegate.onContent(chunk);
    }

    @Override
    public void onThinking(String chunk) {
        delegate.onThinking(chunk);
    }

    @Override
    public void onComplete() {
        if (StrUtil.isNotBlank(sourceAppendix)) {
            delegate.onContent(sourceAppendix);
        }
        delegate.onComplete();
    }

    @Override
    public void onError(Throwable t) {
        delegate.onError(t);
    }
}

