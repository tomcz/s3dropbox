/*
 * Copyright (c) 2010, Thomas Czarniecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of S3DropBox, Thomas Czarniecki, tomczarniecki.com nor
 *    the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tomczarniecki.s3.gui;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

class Announcer<T> {

    private final List<T> targets = new ArrayList<>();
    private final T proxy;

    private Announcer(Class<T> type) {
        this.proxy = proxyFor(type);
    }

    public static <T> Announcer<T> createFor(Class<T> type) {
        return new Announcer<>(type);
    }

    public void add(T target) {
        targets.add(target);
    }

    public T announce() {
        return proxy;
    }

    private T proxyFor(Class<T> type) {
        //noinspection rawtypes
        Class[] proxyInterfaces = {type};
        ClassLoader classLoader = type.getClassLoader();
        return type.cast(Proxy.newProxyInstance(classLoader, proxyInterfaces, (proxy, method, args) -> {
            for (T target : targets) {
                method.invoke(target, args);
            }
            //not mocking out equals here, so let's stop the nagging
            //noinspection SuspiciousInvocationHandlerImplementation
            return null;
        }));
    }
}
