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
package io.github.jleblanc64.hibernate5.hibernate.duplicate;

import com.google.common.collect.Sets;
import org.hibernate.HibernateException;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MyPersistentSet extends AbstractPersistentCollection {
    protected Set set;
    protected transient List tempList;

    public MyPersistentSet(SharedSessionContractImplementor session, Collection coll) {
        super(session);

        if (coll != null) {
            set = Sets.newHashSet(coll);
            setInitialized();
            setDirectlyAccessible(true);
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Serializable getSnapshot(CollectionPersister persister) throws HibernateException {
        final HashMap clonedSet = new HashMap(set.size());
        for (Object aSet : set) {
            final Object copied = persister.getElementType().deepCopy(aSet, persister.getFactory());
            clonedSet.put(copied, copied);
        }
        return clonedSet;
    }

    @Override
    public Collection getOrphans(Serializable snapshot, String entityName) throws HibernateException {
        final java.util.Map sn = (java.util.Map) snapshot;
        return getOrphans(sn.keySet(), set, entityName, getSession());
    }

    @Override
    public boolean equalsSnapshot(CollectionPersister persister) throws HibernateException {
        final Type elementType = persister.getElementType();
        final java.util.Map sn = (java.util.Map) getSnapshot();
        if (sn.size() != set.size()) {
            return false;
        } else {
            for (Object test : set) {
                final Object oldValue = sn.get(test);
                if (oldValue == null || elementType.isDirty(oldValue, test, getSession())) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean isSnapshotEmpty(Serializable snapshot) {
        return ((java.util.Map) snapshot).isEmpty();
    }

    @Override
    public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
        this.set = (Set) persister.getCollectionType().instantiate(anticipatedSize);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initializeFromCache(CollectionPersister persister, Serializable disassembled, Object owner)
            throws HibernateException {
        final Serializable[] array = (Serializable[]) disassembled;
        final int size = array.length;
        beforeInitialize(persister, size);
        for (Serializable arrayElement : array) {
            final Object assembledArrayElement = persister.getElementType().assemble(arrayElement, getSession(), owner);
            if (assembledArrayElement != null) {
                set.add(assembledArrayElement);
            }
        }
    }

    @Override
    public boolean empty() {
        return set.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public int size() {
        return readSize() ? getCachedSize() : set.size();
    }

    @SuppressWarnings("unchecked")
    public boolean isEmpty() {
        return readSize() ? getCachedSize() == 0 : set.isEmpty();
    }

    public boolean contains(Object object) {
        final Boolean exists = readElementExistence(object);
        return exists == null
                ? set.contains(object)
                : exists;
    }

    public Iterator iteratorPriv() {
        read();
        return new IteratorProxy(set.iterator());
    }

    public Object[] toArrayPriv() {
        read();
        return set.toArray();
    }

    public Object[] toArray(Object[] array) {
        read();
        return set.toArray(array);
    }

    public boolean addPriv(Object value) {
        final Boolean exists = isOperationQueueEnabled() ? readElementExistence(value) : null;
        if (exists == null) {
            initialize(true);
            if (set.add(value)) {
                dirty();
                return true;
            } else {
                return false;
            }
        } else if (exists) {
            return false;
        } else {
            queueOperation(new SimpleAdd(value));
            return true;
        }
    }

    public boolean removePriv(Object value) {
        final Boolean exists = isPutQueueEnabled() ? readElementExistence(value) : null;
        if (exists == null) {
            initialize(true);
            if (set.remove(value)) {
                elementRemoved = true;
                dirty();
                return true;
            } else {
                return false;
            }
        } else if (exists) {
            elementRemoved = true;
            queueOperation(new SimpleRemove(value));
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean containsAll(Collection coll) {
        read();
        return set.containsAll(coll);
    }

    @SuppressWarnings("unchecked")
    public boolean addAll(Collection coll) {
        if (coll.size() > 0) {
            initialize(true);
            if (set.addAll(coll)) {
                dirty();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean retainAll(Collection coll) {
        initialize(true);
        if (set.retainAll(coll)) {
            dirty();
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean removeAll(Collection coll) {
        if (coll.size() > 0) {
            initialize(true);
            if (set.removeAll(coll)) {
                elementRemoved = true;
                dirty();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        if (isClearQueueEnabled()) {
            queueOperation(new Clear());
        } else {
            initialize(true);
            if (!set.isEmpty()) {
                set.clear();
                dirty();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        read();
        return set.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object readFrom(
            ResultSet rs,
            CollectionPersister persister,
            CollectionAliases descriptor,
            Object owner) throws HibernateException, SQLException {
        final Object element = persister.readElement(rs, owner, descriptor.getSuffixedElementAliases(), getSession());
        if (element != null) {
            tempList.add(element);
        }
        return element;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void beginRead() {
        super.beginRead();
        tempList = new ArrayList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean endRead() {
        set.addAll(tempList);
        tempList = null;
        // ensure that operationQueue is considered
        return super.endRead();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator entries(CollectionPersister persister) {
        return set.iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Serializable disassemble(CollectionPersister persister) throws HibernateException {
        final Serializable[] result = new Serializable[set.size()];
        final Iterator itr = set.iterator();
        int i = 0;
        while (itr.hasNext()) {
            result[i++] = persister.getElementType().disassemble(itr.next(), getSession(), null);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator getDeletes(CollectionPersister persister, boolean indexIsFormula) throws HibernateException {
        final Type elementType = persister.getElementType();
        final java.util.Map sn = (java.util.Map) getSnapshot();
        final ArrayList deletes = new ArrayList(sn.size());

        Iterator itr = sn.keySet().iterator();
        while (itr.hasNext()) {
            final Object test = itr.next();
            if (!set.contains(test)) {
                // the element has been removed from the set
                deletes.add(test);
            }
        }

        itr = set.iterator();
        while (itr.hasNext()) {
            final Object test = itr.next();
            final Object oldValue = sn.get(test);
            if (oldValue != null && elementType.isDirty(test, oldValue, getSession())) {
                // the element has changed
                deletes.add(oldValue);
            }
        }

        return deletes.iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean needsInserting(Object entry, int i, Type elemType) throws HibernateException {
        final Object oldValue = ((java.util.Map) getSnapshot()).get(entry);
        // note that it might be better to iterate the snapshot but this is safe,
        // assuming the user implements equals() properly, as required by the Set
        // contract!
        return (oldValue == null && entry != null) || elemType.isDirty(oldValue, entry, getSession());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean needsUpdating(Object entry, int i, Type elemType) {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isRowUpdatePossible() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getIndex(Object entry, int i, CollectionPersister persister) {
        throw new UnsupportedOperationException("Sets don't have indexes");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getElement(Object entry) {
        return entry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getSnapshotElement(Object entry, int i) {
        throw new UnsupportedOperationException("Sets don't support updating by element");
    }

    @Override
    @SuppressWarnings({"unchecked", "EqualsWhichDoesntCheckParameterClass"})
    public boolean equals(Object other) {
        read();
        return set.equals(other);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int hashCode() {
        read();
        return set.hashCode();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean entryExists(Object key, int i) {
        return key != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isWrapper(Object collection) {
        return set == collection;
    }

    final class Clear implements DelayedOperation {
        @Override
        public void operate() {
            set.clear();
        }

        @Override
        public Object getAddedInstance() {
            return null;
        }

        @Override
        public Object getOrphan() {
            throw new UnsupportedOperationException("queued clear cannot be used with orphan delete");
        }
    }

    final class SimpleAdd extends AbstractValueDelayedOperation {

        public SimpleAdd(Object addedValue) {
            super(addedValue, null);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void operate() {
            set.add(getAddedInstance());
        }
    }

    final class SimpleRemove extends AbstractValueDelayedOperation {

        public SimpleRemove(Object orphan) {
            super(null, orphan);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void operate() {
            set.remove(getOrphan());
        }
    }
}
