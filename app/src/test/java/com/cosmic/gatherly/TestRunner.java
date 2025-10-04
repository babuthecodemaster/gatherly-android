package com.cosmic.gatherly;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.cosmic.gatherly.ui.adapters.ChannelAdapterTest;
import com.cosmic.gatherly.ui.adapters.ServerAdapterTest;
import com.cosmic.gatherly.ui.util.FileUploadValidationTest;
import com.cosmic.gatherly.ui.util.SearchFunctionalityTest;
import com.cosmic.gatherly.ui.util.ComponentErrorHandlerTest;
import com.cosmic.gatherly.data.manager.VoiceChannelManagerTest;

/**
 * Test suite for all unit tests created for task 11
 * This runner executes all the unit tests for the new functionality
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ChannelAdapterTest.class,
    ServerAdapterTest.class,
    FileUploadValidationTest.class,
    SearchFunctionalityTest.class,
    ComponentErrorHandlerTest.class,
    VoiceChannelManagerTest.class
})
public class TestRunner {
    // This class remains empty, it is used only as a holder for the above annotations
}