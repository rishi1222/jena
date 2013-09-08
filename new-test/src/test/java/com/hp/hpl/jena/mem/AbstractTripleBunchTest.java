/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.mem;

import static org.junit.Assert.*;
import static com.hp.hpl.jena.testing_framework.GraphTestUtils.*;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.hp.hpl.jena.graph.NodeCreateUtils;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Test triple bunch implementations - NOT YET FINISHED
 */
public abstract class AbstractTripleBunchTest {
	protected static final TripleBunch emptyBunch = new ArrayBunch();
	protected static final Triple tripleSPO = triple("s P o");
	protected static final Triple tripleXQY = triple("x Q y");

	protected abstract TripleBunch getBunch();

	@Test
	public void testEmptyBunch() {
		TripleBunch b = getBunch();
		assertEquals(0, b.size());
		assertFalse(b.contains(tripleSPO));
		assertFalse(b.contains(tripleXQY));
		assertFalse(b.iterator().hasNext());
	}

	@Test
	public void testAddElement() {
		TripleBunch b = getBunch();
		b.add(tripleSPO);
		assertEquals(1, b.size());
		assertTrue(b.contains(tripleSPO));
		assertEquals(listOf(tripleSPO), iteratorToList(b.iterator()));
	}

	@Test
	public void testAddElements() {
		TripleBunch b = getBunch();
		b.add(tripleSPO);
		b.add(tripleXQY);
		assertEquals(2, b.size());
		assertTrue(b.contains(tripleSPO));
		assertTrue(b.contains(tripleXQY));
		assertEquals(setOf(tripleSPO, tripleXQY), iteratorToSet(b.iterator()));
	}

	@Test
	public void testRemoveOnlyElement() {
		TripleBunch b = getBunch();
		b.add(tripleSPO);
		b.remove(tripleSPO);
		assertEquals(0, b.size());
		assertFalse(b.contains(tripleSPO));
		assertFalse(b.iterator().hasNext());
	}

	@Test
	public void testRemoveFirstOfTwo() {
		TripleBunch b = getBunch();
		b.add(tripleSPO);
		b.add(tripleXQY);
		b.remove(tripleSPO);
		assertEquals(1, b.size());
		assertFalse(b.contains(tripleSPO));
		assertTrue(b.contains(tripleXQY));
		assertEquals(listOf(tripleXQY), iteratorToList(b.iterator()));
	}

	@Test
	public void testTableGrows() {
		TripleBunch b = getBunch();
		b.add(tripleSPO);
		b.add(tripleXQY);
		b.add(triple("a I b"));
		b.add(triple("c J d"));
	}

	@Test
	public void testIterator() {
		TripleBunch b = getBunch();
		b.add(triple("a P b"));
		b.add(triple("c Q d"));
		b.add(triple("e R f"));
		assertEquals(tripleSet("a P b; c Q d; e R f"), b.iterator().toSet());
	}

	@Test
	public void testIteratorRemoveOneItem() {
		TripleBunch b = getBunch();
		b.add(triple("a P b"));
		b.add(triple("c Q d"));
		b.add(triple("e R f"));
		ExtendedIterator<Triple> it = b.iterator();
		while (it.hasNext())
			if (it.next().equals(triple("c Q d")))
				it.remove();
		assertEquals(tripleSet("a P b; e R f"), b.iterator().toSet());
	}

	@Test
	public void testIteratorRemoveAlltems() {
		TripleBunch b = getBunch();
		b.add(triple("a P b"));
		b.add(triple("c Q d"));
		b.add(triple("e R f"));
		ExtendedIterator<Triple> it = b.iterator();
		while (it.hasNext())
			it.removeNext();
		assertEquals(tripleSet(""), b.iterator().toSet());
	}

	protected List<Triple> listOf(Triple x) {
		List<Triple> result = new ArrayList<Triple>();
		result.add(x);
		return result;
	}

	protected Set<Triple> setOf(Triple x, Triple y) {
		Set<Triple> result = setOf(x);
		result.add(y);
		return result;
	}

	protected Set<Triple> setOf(Triple x) {
		Set<Triple> result = new HashSet<Triple>();
		result.add(x);
		return result;
	}

	public void testAddThenNextThrowsCME() {
		TripleBunch b = getBunch();
		b.add(NodeCreateUtils.createTriple("a P b"));
		b.add(NodeCreateUtils.createTriple("c Q d"));
		ExtendedIterator<Triple> it = b.iterator();
		it.next();
		b.add(NodeCreateUtils.createTriple("change its state"));
		try {
			it.next();
			fail("should have thrown ConcurrentModificationException");
		} catch (ConcurrentModificationException e) {
			// expected
		}
	}

	public void testDeleteThenNextThrowsCME() {
		TripleBunch b = getBunch();
		b.add(NodeCreateUtils.createTriple("a P b"));
		b.add(NodeCreateUtils.createTriple("c Q d"));
		ExtendedIterator<Triple> it = b.iterator();
		it.next();
		b.remove(NodeCreateUtils.createTriple("a P b"));
		try {
			it.next();
			fail("should have thrown ConcurrentModificationException");
		} catch (ConcurrentModificationException e) {
			// expected
		}
	}
}