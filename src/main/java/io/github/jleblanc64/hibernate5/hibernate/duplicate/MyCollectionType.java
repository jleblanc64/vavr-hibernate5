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

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.*;

public class MyCollectionType {

    public static Object replaceElements(
            Object original,
            Object target,
            Object owner,
            Map copyCache,
            SharedSessionContractImplementor session,
            CollectionType c) {

        clear(target);

        // copy elements into newly empty target collection
        Type elemType = c.getElementType(session.getFactory());
        var iter = iterator(original);
        while (iter.hasNext()) {
            add(target, elemType.replace(iter.next(), null, session, owner, copyCache));
        }

        // if the original is a PersistentCollection, and that original
        // was not flagged as dirty, then reset the target's dirty flag
        // here after the copy operation.
        // </p>
        // One thing to be careful of here is a "bare" original collection
        // in which case we should never ever ever reset the dirty flag
        // on the target because we simply do not know...
        if (original instanceof PersistentCollection) {
            if (target instanceof PersistentCollection) {
                final PersistentCollection originalPersistentCollection = (PersistentCollection) original;
                final PersistentCollection resultPersistentCollection = (PersistentCollection) target;

                preserveSnapshot(originalPersistentCollection, resultPersistentCollection, elemType, owner, copyCache, session);

                if (!originalPersistentCollection.isDirty()) {
                    resultPersistentCollection.clearDirty();
                }
            }
        }

        return target;
    }

    private static void clear(Object l) {
        if (l instanceof Collection)
            ((Collection) l).clear();
        else if (l instanceof MyPersistentBag)
            ((MyPersistentBag) l).clear();
        else
            throw new RuntimeException("Not implemented");
    }

    private static Iterator iterator(Object l) {
        if (l instanceof Collection)
            return ((Collection) l).iterator();
        else if (l instanceof MyPersistentBag)
            return ((MyPersistentBag) l).iteratorPriv();
        else
            throw new RuntimeException("Not implemented");
    }

    private static void add(Object l, Object o) {
        if (l instanceof Collection)
            ((Collection) l).add(o);
        else if (l instanceof MyPersistentBag)
            ((MyPersistentBag) l).add(o);
        else
            throw new RuntimeException("Not implemented");
    }

    private static void preserveSnapshot(
            PersistentCollection original,
            PersistentCollection result,
            Type elemType,
            Object owner,
            Map copyCache,
            SharedSessionContractImplementor session) {
        Serializable originalSnapshot = original.getStoredSnapshot();
        Serializable resultSnapshot = result.getStoredSnapshot();
        Serializable targetSnapshot;

        if (originalSnapshot instanceof List) {
            targetSnapshot = new ArrayList(
                    ((List) originalSnapshot).size());
            for (Object obj : (List) originalSnapshot) {
                ((List) targetSnapshot).add(elemType.replace(obj, null, session, owner, copyCache));
            }

        } else if (originalSnapshot instanceof MyPersistentBag) {
            targetSnapshot = new ArrayList(
                    ((MyPersistentBag) originalSnapshot).size());
            var iter = ((MyPersistentBag) originalSnapshot).iteratorPriv();
            while (iter.hasNext()) {
                Object obj = iter.next();
                ((List) targetSnapshot).add(elemType.replace(obj, null, session, owner, copyCache));
            }
        } else if (originalSnapshot instanceof Map) {
            if (originalSnapshot instanceof SortedMap) {
                targetSnapshot = new TreeMap(((SortedMap) originalSnapshot).comparator());
            } else {
                targetSnapshot = new HashMap(
                        CollectionHelper.determineProperSizing(((Map) originalSnapshot).size()),
                        CollectionHelper.LOAD_FACTOR
                );
            }

            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) originalSnapshot).entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                Object resultSnapshotValue = (resultSnapshot == null)
                        ? null
                        : ((Map<Object, Object>) resultSnapshot).get(key);

                Object newValue = elemType.replace(value, resultSnapshotValue, session, owner, copyCache);

                if (key == value) {
                    ((Map) targetSnapshot).put(newValue, newValue);

                } else {
                    ((Map) targetSnapshot).put(key, newValue);
                }

            }

        } else if (originalSnapshot instanceof Object[]) {
            Object[] arr = (Object[]) originalSnapshot;
            for (int i = 0; i < arr.length; i++) {
                arr[i] = elemType.replace(arr[i], null, session, owner, copyCache);
            }
            targetSnapshot = originalSnapshot;

        } else {
            // retain the same snapshot
            targetSnapshot = resultSnapshot;

        }

        CollectionEntry ce = session.getPersistenceContextInternal().getCollectionEntry(result);
        if (ce != null) {
            ce.resetStoredSnapshot(result, targetSnapshot);
        }

    }
}
