import Flutter
import UIKit
import InstanaAgent

public class SwiftInstanaFlutterPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_agent", binaryMessenger: registrar.messenger())
    let instance = SwiftInstanaFlutterPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
    Instana.setup(key: InstanaKey, reportingURL: URL(string: InstanaURL)!)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    if call.method == "getSessionID" {
      result("Instana Session " + (Instana.sessionID ?? "No Session"))
    } else if call.method == "getPlatformVersion" {
      result("iOS " + UIDevice.current.systemVersion)
    } else if call.method == "setView" {
      guard let args = call.arguments as? [String: Any], let name = args["name"] as? String else {
        return result(FlutterError(code: "UNAVAILABLE",
                        message: "Arguments missing",
                        details: nil))
      }
      Instana.setView(name: name)
      result("View name set")
    }
  }
}

let InstanaKey = "$(InstanaKey)"
let InstanaURL = "$(InstanaURL)"
