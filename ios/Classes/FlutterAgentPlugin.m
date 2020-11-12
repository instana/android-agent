#import "FlutterAgentPlugin.h"
#if __has_include(<flutter_agent/flutter_agent-Swift.h>)
#import <flutter_agent/flutter_agent-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_agent-Swift.h"
#endif

@implementation FlutterAgentPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterAgentPlugin registerWithRegistrar:registrar];
}
@end
