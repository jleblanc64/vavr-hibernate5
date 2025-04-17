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

import com.google.common.collect.Streams;
import io.github.jleblanc64.hibernate5.hibernate.duplicate.MyPersistentSet;
import io.vavr.PartialFunction;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.HashSet;
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

    public PersistentSetImpl(SharedSessionContractImplementor session, Collection coll) {
        super(session, coll);
    }

    @Override
    public java.util.Set toJavaSet() {
        return newHashSet((iteratorPriv()));
    }

    @Override
    public Iterator iterator() {
        return Iterator.ofAll(iteratorPriv());
    }

    HashSet h() {
        return HashSet.ofAll(Streams.stream(iteratorPriv()));
    }

    @Override
    public Set add(Object x) {
        addPriv(x);
        return this;
    }

    @Override
    public Set addAll(Iterable elements) {
        super.addAll(newHashSet(elements));
        return this;
    }

    @Override
    public int length() {
        return size();
    }

    @Override
    public Set remove(Object element) {
        removePriv(element);
        return this;
    }

    @Override
    public Set removeAll(Iterable elements) {
        super.removeAll(toJavaSet());
        return this;
    }

    // not implemented
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

    // forward to vavr HashSet
    @Override
    public Set diff(Set that) {
        return h().diff(that);
    }

    @Override
    public Set intersect(Set that) {
        return h().intersect(that);
    }

    @Override
    public Set union(Set that) {
        return h().union(that);
    }

    @Override
    public Set distinct() {
        return h().distinct();
    }

    @Override
    public Set distinctBy(Comparator comparator) {
        return h().distinctBy(comparator);
    }

    @Override
    public Set drop(int n) {
        return h().drop(n);
    }

    @Override
    public Set dropRight(int n) {
        return h().dropRight(n);
    }

    @Override
    public Set dropUntil(Predicate predicate) {
        return h().dropUntil(predicate);
    }

    @Override
    public Set dropWhile(Predicate predicate) {
        return h().dropWhile(predicate);
    }

    @Override
    public Set filter(Predicate predicate) {
        return h().filter(predicate);
    }

    @Override
    public Set reject(Predicate predicate) {
        return h().reject(predicate);
    }

    @Override
    public Object foldRight(Object zero, BiFunction f) {
        return h().foldRight(zero, f);
    }

    @Override
    public Iterator<? extends Set> grouped(int size) {
        return h().grouped(size);
    }

    @Override
    public boolean hasDefiniteSize() {
        return h().hasDefiniteSize();
    }

    @Override
    public Object head() {
        return h().head();
    }

    @Override
    public Set init() {
        return h().init();
    }

    @Override
    public Option<? extends Set> initOption() {
        return h().initOption();
    }

    @Override
    public boolean isTraversableAgain() {
        return h().isTraversableAgain();
    }

    @Override
    public Object last() {
        return h().last();
    }

    @Override
    public Set orElse(Iterable other) {
        return h().orElse(other);
    }

    @Override
    public Tuple2<? extends Set, ? extends Set> partition(Predicate predicate) {
        return h().partition(predicate);
    }

    @Override
    public boolean isAsync() {
        return h().isAsync();
    }

    @Override
    public boolean isLazy() {
        return h().isLazy();
    }

    @Override
    public Set peek(Consumer action) {
        return h().peek(action);
    }

    @Override
    public String stringPrefix() {
        return h().stringPrefix();
    }

    @Override
    public Set scan(Object zero, BiFunction operation) {
        return h().scan(zero, operation);
    }

    @Override
    public Iterator<? extends Set> sliding(int size) {
        return h().sliding(size);
    }

    @Override
    public Iterator<? extends Set> sliding(int size, int step) {
        return h().sliding(size, step);
    }

    @Override
    public Tuple2<? extends Set, ? extends Set> span(Predicate predicate) {
        return h().span(predicate);
    }

    @Override
    public Set tail() {
        return h().tail();
    }

    @Override
    public Option<? extends Set> tailOption() {
        return h().tailOption();
    }

    @Override
    public Set take(int n) {
        return h().take(n);
    }

    @Override
    public Set takeRight(int n) {
        return h().takeRight(n);
    }

    @Override
    public Set takeUntil(Predicate predicate) {
        return h().takeUntil(predicate);
    }

    @Override
    public Set takeWhile(Predicate predicate) {
        return h().takeWhile(predicate);
    }

    @Override
    public Set<Tuple2> zipWithIndex() {
        return h().zipWithIndex();
    }

    @Override
    public Set zipWithIndex(BiFunction mapper) {
        return h().zipWithIndex(mapper);
    }

    @Override
    public Set<Tuple2> zipAll(Iterable that, Object thisElem, Object thatElem) {
        return h().zipAll(that, thisElem, thatElem);
    }

    @Override
    public Set zipWith(Iterable that, BiFunction mapper) {
        return h().zipWith(that, mapper);
    }

    @Override
    public Set<Tuple2> zip(Iterable that) {
        return h().zip(that);
    }

    @Override
    public Tuple3<? extends Set, ? extends Set, ? extends Set> unzip3(Function unzipper) {
        return h().unzip3(unzipper);
    }

    @Override
    public Tuple2<? extends Set, ? extends Set> unzip(Function unzipper) {
        return h().unzip(unzipper);
    }

    @Override
    public Iterator<? extends Set> slideBy(Function classifier) {
        return h().slideBy(classifier);
    }

    @Override
    public Set scanRight(Object zero, BiFunction operation) {
        return h().scanRight(zero, operation);
    }

    @Override
    public Set scanLeft(Object zero, BiFunction operation) {
        return h().scanLeft(zero, operation);
    }

    @Override
    public Set orElse(Supplier supplier) {
        return h().orElse(supplier);
    }

    @Override
    public Set map(Function mapper) {
        return h().map(mapper);
    }

    @Override
    public Map groupBy(Function classifier) {
        return h().groupBy(classifier);
    }

    @Override
    public Set flatMap(Function mapper) {
        return h().flatMap(mapper);
    }

    @Override
    public Set distinctBy(Function keyExtractor) {
        return h().distinctBy(keyExtractor);
    }

    @Override
    public Set collect(PartialFunction partialFunction) {
        return h().collect(partialFunction);
    }
}