import Foundation

struct KickMomentFile: Codable {
    var moments: [KickMomentRecord]
}

struct KickMomentRecord: Codable, Identifiable {
    let id: String
    let timestampMillis: Int64
}
