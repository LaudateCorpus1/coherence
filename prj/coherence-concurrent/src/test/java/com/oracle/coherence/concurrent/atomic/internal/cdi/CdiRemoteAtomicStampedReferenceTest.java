/*
 * Copyright (c) 2020, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.concurrent.atomic.internal.cdi;

import com.oracle.coherence.cdi.CoherenceExtension;

import com.oracle.coherence.cdi.server.CoherenceServerExtension;

import com.oracle.coherence.concurrent.atomic.AtomicStampedReference;
import com.oracle.coherence.concurrent.atomic.AtomicStampedReferenceTest;
import com.oracle.coherence.concurrent.atomic.RemoteAtomicStampedReference;

import com.oracle.coherence.cdi.Name;
import com.oracle.coherence.cdi.Remote;
import com.oracle.coherence.cdi.SessionName;

import com.tangosol.net.NamedMap;

import javax.inject.Inject;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link RemoteAtomicStampedReference} CDI tests.
 * 
 * @author Aleks Seovic  2020.12.09
 */
@ExtendWith(WeldJunit5Extension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CdiRemoteAtomicStampedReferenceTest
        extends AtomicStampedReferenceTest
    {
    @WeldSetup
    private WeldInitiator weld = WeldInitiator.of(WeldInitiator.createWeld()
                                                          .addExtension(new CoherenceExtension())
                                                          .addPackages(CoherenceExtension.class)
                                                          .addPackages(CoherenceServerExtension.class)
                                                          .addBeanClass(AtomicStampedReferenceProducer.class));

    @Inject
    @Remote
    AtomicStampedReference<String> value;

    @Inject
    @SessionName("concurrent")
    @Name("atomic-stamped-ref")
    NamedMap<String, java.util.concurrent.atomic.AtomicStampedReference<String>> refs;

    @Test
    @Order(100)
    void testRemoteEntry()
        {
        int[] aStamp = new int[1];
        assertThat(refs.get("value").get(aStamp), is("atomic"));
        assertThat(aStamp[0], is(3));
        }

    protected AtomicStampedReference<String> value()
        {
        return value;
        }
    }
