/*
 * Copyright (c) 2000, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.tangosol.net.topic;


import com.tangosol.internal.net.topic.impl.paged.model.SubscriberGroupId;
import com.tangosol.net.NamedCollection;

import com.tangosol.util.Binary;
import com.tangosol.util.ClassHelper;
import com.tangosol.util.Filter;

import java.util.Set;
import java.util.function.Function;

/**
 * NamedTopic represents a topic entity for publish/subscribe messaging.
 * <p>
 * A topic may have any number of {@link Publisher publishers} or {@link Subscriber subscribers}.
 * {@link Subscriber}s subscribe directly to the topic or subscribe to a logical {@link Subscriber.Name subscriber group} of the topic.
 * Each published value to a topic is delivered to all direct topic {@link Subscriber}s and
 * each subscriber group of the topic. Each value of a subscriber group is only consumed by one {@link Subscriber subscriber group member}.
 * Thus each subscriber group in effect behaves like a queue over the topic data.
 * <p>
 * Once {@link Publisher#publish published}, a value will be retained by the topic until it has either
 * expired or has been {@link Subscriber#receive received} by all {@link Subscriber.Name subscriber group(s)}
 * and direct topic {@link Subscriber}(s) which were registered prior to it being published.
 * The ordering of values within the topic is dependent on the {@link Publisher.OrderBy} option.
 * <p>
 * <b>Subscriber Group Lifecycle</b><br>
 * A subscriber group is created either at configuration time or dynamically.
 * A <tt>subscriber-group</tt> child element of a <tt>topic-mapping</tt> element in a cache configuration file statically
 * configures a subscriber group when the topic is created, ensuring all values published to the
 * topic are also delivered to the statically configured subscriber group.
 * A subscriber group is created dynamically when a {@link NamedTopic#createSubscriber(Subscriber.Option[]) createSubscriber call} specifies a
 * subscriber group name using the option {@link Subscriber.Name#of(String)} and the subscriber group does not exist on the {@link NamedTopic}.
 * <p>
 * One must actively manage a {@link NamedTopic#getSubscriberGroups() NamedTopic's logical subscriber groups} since their life span
 * is independent of their active {@link Subscriber} membership.
 * {@link NamedTopic#destroySubscriberGroup(String)} releases storage and stops accumulating topic values for a subscriber group.
 * <p>
 * To release storage resources for unconsumed values for a direct topic {@link Subscriber}, it is sufficient to ensure it is {@link Subscriber#close() closed}.
 * Both topic {@link Publisher} and {@link Subscriber} can be defined with the try-with-resource pattern to ensure their resources are
 * closed when no longer in scope.
 *
 * @param <V>  the type of the topic values
 *
 * @author jf/jk/mf 2015.06.03
 * @since Coherence 14.1.1
 *
 * @see Publisher
 * @see Subscriber
 */
@SuppressWarnings("unchecked")
public interface NamedTopic<V>
        extends NamedCollection
    {
    // ----- NamedTopic interface -------------------------------------------

    /**
     * Create a {@link Publisher} that can publish values into this {@link NamedTopic}.
     *
     * @param options  the {@link Publisher.Option}s controlling the {@link Publisher}
     *
     * @return a {@link Publisher} that can publish values into this {@link NamedTopic}
     */
    public Publisher<V> createPublisher(Publisher.Option<? super V>... options);

    /**
     * Create a {@link Publisher} that can publish values into this {@link NamedTopic}.
     *
     * @return a {@link Publisher} that can publish values into this {@link NamedTopic}
     */
    public default Publisher<V> createPublisher()
        {
        // this method variant only exists so that callers which don't specify options won't get
        // a compile time warning about the implicit generic array creation
        return createPublisher(new Publisher.Option[0]);
        }

    /**
     * Create a {@link Subscriber} that can receive values from this {@link NamedTopic}.
     *
     * @param options  the {@link Subscriber.Option}s controlling the {@link Subscriber}
     *
     * @return a {@link Subscriber} that can receive values from this {@link NamedTopic}
     */
    public <U> Subscriber<U> createSubscriber(Subscriber.Option<? super V, U>... options);

    /**
     * Create a direct {@link Subscriber} to the topic that receives all values from this {@link NamedTopic}.
     *
     * @return a {@link Subscriber} that can receive values from this {@link NamedTopic}
     */
    public default Subscriber<V> createSubscriber()
        {
        // this method variant only exists so that callers which don't specify options won't get
        // a compile time warning about the implicit generic array creation
        return createSubscriber(new Subscriber.Option[0]);
        }

    /**
     * Ensure that the specified subscriber group exists for this topic.
     *
     * @param sName  the name of the subscriber group
     *
     * @throws IllegalStateException if the subscriber group already exists with a different filter
     *                               or converter function
     */
    public default void ensureSubscriberGroup(String sName)
        {
        ensureSubscriberGroup(sName, null, null);
        }

    /**
     * Ensure that the specified subscriber group exists for this topic.
     *
     * @param sName        the name of the subscriber group
     * @param filter       the {@link Filter} used to filter messages to be received by subscribers in the group
     * @param fnConverter  the {@link Function} used to convert messages to be received by subscribers in the group
     *
     * @throws IllegalStateException if the subscriber group already exists with a different filter
     *                               or converter function
     */
    public void ensureSubscriberGroup(String sName, Filter<?> filter, Function<?, ?> fnConverter);

    /**
     * Destroy the {@link Subscriber.Name named} subscriber group for the associated topic.
     * <p>
     * Releases storage and stops accumulating topic values for destroyed subscriber group.
     * This operation will impact all {@link Subscriber members} of the subscriber group.
     */
    void destroySubscriberGroup(String sGroupName);

    /**
     * Return the set of {@link Subscriber.Name named} subscriber group(s) and statically configured subscriber-group(s).
     *
     * @return the set of named subscriber groups.
     */
    Set<String> getSubscriberGroups();

    /**
     * Specifies whether or not this NamedTopic has been destroyed.
     *
     * Implementations must override this method to provide the necessary information.
     *
     * @return true if the NamedTopic has been destroyed; false otherwise
     */
    public default boolean isDestroyed()
        {
        // to avoid cumbersome caller exception handling;
        // default is a no-op.
        return false;
        }

    /**
     * Specifies whether or not this NamedTopic has been released.
     *
     * Implementations must override this method to provide the necessary information.
     *
     * @return true if the NamedTopic has been released; false otherwise
     */
    public default boolean isReleased()
        {
        // to avoid cumbersome caller exception handling;
        // default is a no-op.
        return false;
        }

    /**
     * Returns the number of channels that this topic has.
     *
     *  @return the number of channels that this topic has
     */
    public int getChannelCount();

    /**
     * Returns the number of remaining messages to be read from the topic for the specific subscriber group.
     * <p>
     * This method is a sum of the remaining messages for each channel from the last committed message (exclusive)
     * to the current tail. This result returned by this method is somewhat transient in situations where there are
     * active Subscribers with in-flight commit requests, so the count may change just after the method returns.
     * Message expiry may also affect the returned value, if messages expire after the count is returned.
     *
     * @param sSubscriberGroup  the name of the subscriber group
     *
     * @return  the number of remaining messages for the subscriber group
     */
    default int getRemainingMessages(String sSubscriberGroup)
        {
        return getRemainingMessages(sSubscriberGroup, new int[0]);
        }

    /**
     * Returns the number of remaining messages to be read from the topic for the specific subscriber group,
     * and optionally for one or more specific channels.
     * <p>
     * This method is a sum of the remaining messages for each channel from the last committed message (exclusive)
     * to the current tail. This result returned by this method is somewhat transient in situations where there are
     * active Subscribers with in-flight commit requests, so the count may change just after the method returns.
     * Message expiry may also affect the returned value, if messages expire after the count is returned.
     *
     * @param sSubscriberGroup  the name of the subscriber group
     * @param anChannel         one or more optional channels
     *
     * @return  the number of remaining messages for the subscriber group
     */
    int getRemainingMessages(String sSubscriberGroup, int... anChannel);

    // ----- inner interface: ElementCalculator -----------------------------

    /**
     * A unit calculator is an object that can calculate the cost of
     * storing an element in a topic.
     */
    interface ElementCalculator
        {
        /**
         * Calculate cost for the specified element.
         *
         * @param binElement  the element value (in serialized Binary form) to evaluate for unit cost
         *
         * @return an integer value 0 or greater, with a larger value
         *         signifying a higher cost
         *
         * @throws IllegalArgumentException if the specified object type
         *         cannot be processed by this calculator
         */
        int calculateUnits(Binary binElement);

        /**
         * Obtain the name of the unit calculator. This is intended to be
         * human readable for use in a monitoring tool; examples include
         * "SimpleMemoryCalculator" and "BinaryMemoryCalculator".
         *
         * @return the name of the unit calculator
         */
        default String getName()
            {
            return ClassHelper.getSimpleName(getClass());
            }
        }
    }
