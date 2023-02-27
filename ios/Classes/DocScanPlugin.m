#import "DocScanPlugin.h"
#if __has_include(<docscan/docscan-Swift.h>)
#import <docscan/docscan-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "docscan-Swift.h"
#endif

@implementation DocScanPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftDocScanPlugin registerWithRegistrar:registrar];
}
@end
