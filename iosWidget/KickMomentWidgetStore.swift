import Foundation

struct KickMomentWidgetStore {
    private let appGroupId = "group.com.punchestracker.KickTracker"
    private let fileName = "kick_moments.json"

    func loadMoments() -> [KickMomentRecord] {
        guard let url = fileUrl(), FileManager.default.fileExists(atPath: url.path) else {
            return []
        }
        do {
            let data = try Data(contentsOf: url)
            let file = try JSONDecoder().decode(KickMomentFile.self, from: data)
            return file.moments.sorted { $0.timestampMillis > $1.timestampMillis }
        } catch {
            return []
        }
    }

    func saveCurrentMoment() throws {
        let current = loadMoments()
        let timestampMillis = Int64(Date().timeIntervalSince1970 * 1000)
        let newRecord = KickMomentRecord(
            id: "kick-\(timestampMillis)-widget",
            timestampMillis: timestampMillis
        )
        let updated = ([newRecord] + current).sorted { $0.timestampMillis > $1.timestampMillis }
        let file = KickMomentFile(moments: updated)
        let data = try JSONEncoder().encode(file)
        guard let url = fileUrl() else {
            throw WidgetStoreError.appGroupUnavailable
        }
        try data.write(to: url, options: [.atomic])
    }

    private func fileUrl() -> URL? {
        FileManager.default
            .containerURL(forSecurityApplicationGroupIdentifier: appGroupId)?
            .appendingPathComponent(fileName)
    }
}

enum WidgetStoreError: Error {
    case appGroupUnavailable
}
