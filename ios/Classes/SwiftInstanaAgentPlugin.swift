import Flutter
import UIKit
import InstanaAgent

enum SwiftInstanaAgentPluginError: Error {
    case missingOrInvalidArgs([String])
    case invalidSetup
    case captureResultFailed(String)

    var code: String {
        switch self {
            case .missingOrInvalidArgs: return "MissingOrInvalidArguments"
            case .invalidSetup: return "InvalidSetup"
            case .captureResultFailed: return "CaptureResultFailed"
        }
    }

    var message: String {
        switch self {
            case .missingOrInvalidArgs(let args): return "Invalid or missing Arguments \(args)"
            case .invalidSetup: return "Instana setup invalid. Please call setup before calling other methods"
            case .captureResultFailed(let val): return "HTTP failed: \(val)"
        }
    }

    var flutterError: FlutterError {
        return FlutterError(code: code, message: message, details: nil)
    }
}

public class SwiftInstanaAgentPlugin: NSObject, FlutterPlugin {
    var markerIDMapper = [String: HTTPMarker]()

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "instana_agent", binaryMessenger: registrar.messenger())
        let instance = SwiftInstanaAgentPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if !verifySetup(call, result) {
            return
        }
        if call.method == "setup" {
            setup(call, result)
        } else if call.method == "setUserID" {
            setUserID(call, result)
        } else if call.method == "setUserName" {
            setUserName(call, result)
        } else if call.method == "setUserEmail" {
            setUserEmail(call, result)
        } else if call.method == "setView" {
            setView(call, result)
        } else if call.method == "setMeta" {
            setMeta(call, result)
        } else if call.method == "reportEvent" {
            reportEvent(call, result)
        } else if call.method == "startCapture" {
            startCapture(call, result)
        } else if call.method == "finish" {
            finish(call, result)
        } else if call.method == "cancel" {
            cancel(call, result)
        } else if call.method == "getView" {
            getView(call, result)
        } else if call.method == "getSessionID" { 
            getSessionID(call, result)
        } else {
            result(FlutterMethodNotImplemented)
        }
    }

    func setup(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        guard let url = url(for: "reportingUrl", at: call),
              let key = string(for: "key", at: call) else {
            return result(SwiftInstanaAgentPluginError.missingOrInvalidArgs(["reportingUrl", "key"]).flutterError)
        }
        Instana.setup(key: key, reportingURL: url, httpCaptureConfig: .manual)
        result("Instana did setup")
    }

    func verifySetup(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) -> Bool {
        if call.method != "setup", Instana.reportingURL == nil || Instana.key == nil {
            result(SwiftInstanaAgentPluginError.invalidSetup.flutterError)
            return false
        }
        return true
    }

    func setUserID(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        guard let userID = string(for: "userID", at: call) else {
            return result(SwiftInstanaAgentPluginError.missingOrInvalidArgs(["userID"]).flutterError)
        }
        Instana.setUser(id: userID)
        result("UserID \(userID) set")
    }

    func setUserName(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        guard let userName = string(for: "userName", at: call) else {
            return result(SwiftInstanaAgentPluginError.missingOrInvalidArgs(["userName"]).flutterError)
        }
        Instana.setUser(name: userName)
        result("User's name \(userName) set")
    }

    func setUserEmail(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        guard let userEmail = string(for: "userEmail", at: call) else {
            return result(SwiftInstanaAgentPluginError.missingOrInvalidArgs(["userEmail"]).flutterError)
        }
        Instana.setUser(email: userEmail)
        result("User's email \(userEmail) set")
    }

    func setView(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        guard let name = string(for: "viewName", at: call) else {
            return result(SwiftInstanaAgentPluginError.missingOrInvalidArgs(["viewName"]).flutterError)
        }
        Instana.setView(name: name)
        result("View \(name) set")
    }

    func setMeta(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        guard let key = string(for: "key", at: call),
              let value = string(for: "value", at: call) else {
            return result(SwiftInstanaAgentPluginError.missingOrInvalidArgs(["key", "value"]).flutterError)
        }
        Instana.setMeta(value: value, key: key)
        result("Meta \(key):\(value) set")
    }

    func reportEvent(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        guard let eventName = string(for: "eventName", at: call) else {
            return result(SwiftInstanaAgentPluginError.missingOrInvalidArgs(["eventName"]).flutterError)
        }
        let startTime = int64(for: "startTime", at: call) ?? Instana.Types.Milliseconds(NSNotFound)
        let duration = int64(for: "duration", at: call) ?? Instana.Types.Milliseconds(NSNotFound)
        let viewName = string(for: "viewName", at: call)
        let backendTracingID = string(for: "backendTracingID", at: call)
        let meta = stringDict(for: "meta", at: call)
        Instana.reportEvent(name: eventName,
                            timestamp: startTime,
                            duration: duration,
                            backendTracingID: backendTracingID,
                            error: nil,
                            meta: meta,
                            viewName: viewName)
        result("Reported event with name \(eventName)")
    }

    func startCapture(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        guard let url = url(for: "url", at: call),
              let method = string(for: "method", at: call) else {
            return result(SwiftInstanaAgentPluginError.missingOrInvalidArgs(["url", "method"]).flutterError)
        }
        let viewName = string(for: "viewName", at: call)
        let marker = Instana.startCapture(url: url, method: method, viewName: viewName)
        let uuid = UUID().uuidString
        markerIDMapper[uuid] = marker
        result(uuid)
    }

    func finish(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        print(call)
        guard let id = string(for: "id", at: call) else {
            return result(SwiftInstanaAgentPluginError.missingOrInvalidArgs(["id"]).flutterError)
        }
        let statusCode = int(for: "responseStatusCode", at: call) ?? 200
        let errorMsg = string(for: "errorMessage", at: call)
        let error = errorMsg != nil ? SwiftInstanaAgentPluginError.captureResultFailed(errorMsg!) : nil
        let backendTracingID = string(for: "backendTracingID", at: call)
        let headerSize = int64(for: "responseSizeHeader", at: call) ?? 0
        let bodySize = int64(for: "responseSizeBody", at: call) ?? 0
        let bodySizeAfterDecoding = int64(for: "responseSizeBodyDecoded", at: call) ?? 0
        let marker = markerIDMapper[id]
        let size: HTTPMarker.Size = HTTPMarker.Size(header: headerSize, body: bodySize, bodyAfterDecoding: bodySizeAfterDecoding)
        let captureResult = HTTPCaptureResult(statusCode: statusCode,
                                       backendTracingID: backendTracingID,
                                       responseSize: size,
                                       error: error)
        marker?.finish(captureResult)
        markerIDMapper[id] = nil
        result("Finish capture for marker id: \(id)")
    }

    func cancel(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        guard let id = string(for: "id", at: call) else {
            return result(SwiftInstanaAgentPluginError.missingOrInvalidArgs(["id"]).flutterError)
        }
        let marker = markerIDMapper[id]
        marker?.cancel()
        markerIDMapper[id] = nil
    }

    func getView(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        result(Instana.viewName)
    }

    func getSessionID(_ call: FlutterMethodCall, _ result: @escaping FlutterResult) {
        result(Instana.sessionID)
    }

    // Helper
    private func value<T>(for key: String, at call: FlutterMethodCall) -> T? {
        guard let args = call.arguments as? [String: Any],
              let value = args[key] as? T else {
            return nil
        }
        return value
    }

    private func string(for key: String, at call: FlutterMethodCall) -> String? {
        value(for: key, at: call) as String?
    }

    private func stringDict(for key: String, at call: FlutterMethodCall) -> [String: String]? {
        value(for: key, at: call) as [String: String]?
    }

    private func stringArray(for key: String, at call: FlutterMethodCall) -> [String]? {
        value(for: key, at: call) as [String]?
    }

    private func int(for key: String, at call: FlutterMethodCall) -> Int? {
        value(for: key, at: call) as Int?
    }

    private func int64(for key: String, at call: FlutterMethodCall) -> Int64? {
        value(for: key, at: call) as Int64?
    }

    private func double(for key: String, at call: FlutterMethodCall) -> Double? {
        value(for: key, at: call) as Double?
    }

    private func url(for key: String, at call: FlutterMethodCall) -> URL? {
        guard let url = URL(string: string(for: key, at: call) ?? "") else {
            return nil
        }
        return url
    }
}
