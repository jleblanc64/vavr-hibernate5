/*
 * Copyright 2024 - Charles Dabadie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.jleblanc64.hibernate5.impl;

import io.github.jleblanc64.hibernate5.hibernate.duplicate.MyPersistentSet;
import io.vavr.PartialFunction;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.*;

import static com.google.common.collect.Sets.newHashSet;

public class PersistentSetImpl extends MyPersistentSet implements Set {
    static RuntimeException NOT_IMPL = new RuntimeException("Not implemented");

    public PersistentSetImpl(SharedSessionContractImplementor session) {
        super(session);
    }

    public PersistentSetImpl(SharedSessionContractImplementor session, Collection coll) {
        super(session, newHashSet(coll.iterator()));
    }

    @Override
    public java.util.Set toJavaSet() {
        return newHashSet((iteratorPriv()));
    }

    @Override
    public Set add(Object element) {
        throw NOT_IMPL;
    }

    @Override
    public Set addAll(Iterable elements) {
        throw NOT_IMPL;
    }

    @Override
    public int length() {
        throw NOT_IMPL;
    }

    @Override
    public Set remove(Object element) {
        throw NOT_IMPL;
    }

    @Override
    public Set removeAll(Iterable elements) {
        throw NOT_IMPL;
    }

    @Override
    public Iterator iterator() {
        throw NOT_IMPL;
    }

    @Override
    public Set diff(Set that) {
        throw NOT_IMPL;
    }

    @Override
    public Set intersect(Set that) {
        throw NOT_IMPL;
    }

    @Override
    public Set union(Set that) {
        throw NOT_IMPL;
    }

    @Override
    public Set distinct() {
        throw NOT_IMPL;
    }

    @Override
    public Set distinctBy(Comparator comparator) {
        throw NOT_IMPL;
    }

    @Override
    public Set drop(int n) {
        throw NOT_IMPL;
    }

    @Override
    public Set dropRight(int n) {
        throw NOT_IMPL;
    }

    @Override
    public Set dropUntil(Predicate predicate) {
        throw NOT_IMPL;
    }

    @Override
    public Set dropWhile(Predicate predicate) {
        throw NOT_IMPL;
    }

    @Override
    public Set filter(Predicate predicate) {
        throw NOT_IMPL;
    }

    @Override
    public Set reject(Predicate predicate) {
        throw NOT_IMPL;
    }

    @Override
    public Object foldRight(Object zero, BiFunction f) {
        throw NOT_IMPL;
    }

    @Override
    public Iterator<? extends Set> grouped(int size) {
        throw NOT_IMPL;
    }

    @Override
    public boolean hasDefiniteSize() {
        throw NOT_IMPL;
    }

    @Override
    public Object head() {
        throw NOT_IMPL;
    }

    @Override
    public Set init() {
        throw NOT_IMPL;
    }

    @Override
    public Option<? extends Set> initOption() {
        throw NOT_IMPL;
    }

    @Override
    public boolean isTraversableAgain() {
        throw NOT_IMPL;
    }

    @Override
    public Object last() {
        throw NOT_IMPL;
    }

    @Override
    public Set orElse(Iterable other) {
        throw NOT_IMPL;
    }

    @Override
    public Tuple2<? extends Set, ? extends Set> partition(Predicate predicate) {
        throw NOT_IMPL;
    }

    @Override
    public boolean isAsync() {
        throw NOT_IMPL;
    }

    @Override
    public boolean isLazy() {
        throw NOT_IMPL;
    }

    @Override
    public Set peek(Consumer action) {
        throw NOT_IMPL;
    }

    @Override
    public String stringPrefix() {
        throw NOT_IMPL;
    }

    @Override
    public Set replace(Object currentElement, Object newElement) {
        throw NOT_IMPL;
    }

    @Override
    public Set replaceAll(Object currentElement, Object newElement) {
        throw NOT_IMPL;
    }

    @Override
    public Set retainAll(Iterable elements) {
        throw NOT_IMPL;
    }

    @Override
    public Set scan(Object zero, BiFunction operation) {
        throw NOT_IMPL;
    }

    @Override
    public Iterator<? extends Set> sliding(int size) {
        throw NOT_IMPL;
    }

    @Override
    public Iterator<? extends Set> sliding(int size, int step) {
        throw NOT_IMPL;
    }

    @Override
    public Tuple2<? extends Set, ? extends Set> span(Predicate predicate) {
        throw NOT_IMPL;
    }

    @Override
    public Set tail() {
        throw NOT_IMPL;
    }

    @Override
    public Option<? extends Set> tailOption() {
        throw NOT_IMPL;
    }

    @Override
    public Set take(int n) {
        throw NOT_IMPL;
    }

    @Override
    public Set takeRight(int n) {
        throw NOT_IMPL;
    }

    @Override
    public Set takeUntil(Predicate predicate) {
        throw NOT_IMPL;
    }

    @Override
    public Set takeWhile(Predicate predicate) {
        throw NOT_IMPL;
    }

    @Override
    public Set<Tuple2> zipWithIndex() {
        throw NOT_IMPL;
    }

    @Override
    public Set zipWithIndex(BiFunction mapper) {
        throw NOT_IMPL;
    }

    @Override
    public Set<Tuple2> zipAll(Iterable that, Object thisElem, Object thatElem) {
        throw NOT_IMPL;
    }

    @Override
    public Set zipWith(Iterable that, BiFunction mapper) {
        throw NOT_IMPL;
    }

    @Override
    public Set<Tuple2> zip(Iterable that) {
        throw NOT_IMPL;
    }

    @Override
    public Tuple3<? extends Set, ? extends Set, ? extends Set> unzip3(Function unzipper) {
        throw NOT_IMPL;
    }

    @Override
    public Tuple2<? extends Set, ? extends Set> unzip(Function unzipper) {
        throw NOT_IMPL;
    }

    @Override
    public Iterator<? extends Set> slideBy(Function classifier) {
        throw NOT_IMPL;
    }

    @Override
    public Set scanRight(Object zero, BiFunction operation) {
        throw NOT_IMPL;
    }

    @Override
    public Set scanLeft(Object zero, BiFunction operation) {
        throw NOT_IMPL;
    }

    @Override
    public Set orElse(Supplier supplier) {
        throw NOT_IMPL;
    }

    @Override
    public Set map(Function mapper) {
        throw NOT_IMPL;
    }

    @Override
    public Map groupBy(Function classifier) {
        throw NOT_IMPL;
    }

    @Override
    public Set flatMap(Function mapper) {
        throw NOT_IMPL;
    }

    @Override
    public Set distinctBy(Function keyExtractor) {
        throw NOT_IMPL;
    }

    @Override
    public Set collect(PartialFunction partialFunction) {
        throw NOT_IMPL;
    }
}
