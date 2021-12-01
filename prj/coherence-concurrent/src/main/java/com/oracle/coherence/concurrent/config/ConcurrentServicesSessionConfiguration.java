/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.concurrent.config;

import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.SessionConfiguration;
import com.tangosol.net.SessionProvider;

import java.util.Optional;

public class ConcurrentServicesSessionConfiguration
        implements SessionConfiguration
    {
    // ----- SessionConfiguration interface ---------------------------------

    @Override
    public String getName()
        {
        return SESSION_NAME;
        }

    @Override
    public String getScopeName()
        {
        return Coherence.SYSTEM_SCOPE;
        }

    @Override
    public Optional<String> getConfigUri()
        {
        return Optional.of(CONFIG_URI);
        }

    // ----- inner class: AtomicsSessionProvider ----------------------------

    /**
     * The custom Atomics session provider.
     */
    public static class ConcurrentServicesSessionProvider
            implements SessionProvider
        {
        // ----- SessionProvider interface ----------------------------------

        @Override
        public Context createSession(SessionConfiguration configuration, Context context)
            {
            if (SESSION_NAME.equals(configuration.getName()))
                {
                if (context.getMode() == Coherence.Mode.ClusterMember)
                    {
                    // we only add this Atomics session on a cluster member
                    return context.createSession(configuration);
                    }
                else
                    {
                    // there is no Atomics session on an Extend client.
                    return context.complete();
                    }
                }
            // the request was not for the Atomics session
            return context;
            }
        }

    // ----- constants ------------------------------------------------------

    /**
     * The name of the {@code coherence-concurrent} {@link Session session}.
     */
    public static String SESSION_NAME = "concurrent";

    /**
     * The URI to the default configuration for the {@code coherence-concurrent}
     * module.
     */
    public static String CONFIG_URI = "concurrent-services.xml";
    }