/**
 * Copyright (c) 2017 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.pravega.segmentstore.server.logs.operations;

import io.pravega.common.io.SerializationException;
import io.pravega.test.common.AssertExtensions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the ProbeOperation class.
 */
public class ProbeOperationTests {

    /**
     * Tests the ability of the ProbeOperation to reject all serialization/deserialization requests.
     */
    @Test
    public void testSerialization() {
        ProbeOperation op = new ProbeOperation();
        Assert.assertFalse("Unexpected value from canSerialize().", op.canSerialize());
        op.setSequenceNumber(1);
        AssertExtensions.assertThrows(
                "serialize() did not fail with the expected exception.",
                () -> OperationSerializer.DEFAULT.serialize(new ByteArrayOutputStream(), op),
                ex -> ex instanceof SerializationException);

        // Even though there is no deserialization constructor, we need to ensure that the deserializeContent method
        // does not work.
        AssertExtensions.assertThrows(
                "deserializeContent() did not fail with the expected exception.",
                () -> OperationSerializer.DEFAULT.deserialize(new ByteArrayInputStream(new byte[100])),
                ex -> ex instanceof SerializationException);
    }
}
