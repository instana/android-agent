/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */
 
#import "InstanaAgentPlugin.h"
#if __has_include(<instana_agent/instana_agent-Swift.h>)
#import <instana_agent/instana_agent-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "instana_agent-Swift.h"
#endif

@implementation InstanaAgentPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftInstanaAgentPlugin registerWithRegistrar:registrar];
}
@end
