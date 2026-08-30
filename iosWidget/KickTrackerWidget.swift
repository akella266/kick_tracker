import AppIntents
import SwiftUI
import WidgetKit

struct RecordKickIntent: AppIntent {
    static var title: LocalizedStringResource = "Записать шевеление"
    static var description = IntentDescription("Сохраняет текущее время шевеления.")

    func perform() async throws -> some IntentResult {
        try KickMomentWidgetStore().saveCurrentMoment()
        WidgetCenter.shared.reloadAllTimelines()
        return .result()
    }
}

struct KickTrackerEntry: TimelineEntry {
    let date: Date
    let lastMoment: KickMomentRecord?
}

struct KickTrackerProvider: TimelineProvider {
    func placeholder(in context: Context) -> KickTrackerEntry {
        KickTrackerEntry(date: Date(), lastMoment: nil)
    }

    func getSnapshot(in context: Context, completion: @escaping (KickTrackerEntry) -> Void) {
        completion(makeEntry())
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<KickTrackerEntry>) -> Void) {
        completion(Timeline(entries: [makeEntry()], policy: .never))
    }

    private func makeEntry() -> KickTrackerEntry {
        KickTrackerEntry(date: Date(), lastMoment: KickMomentWidgetStore().loadMoments().first)
    }
}

struct KickTrackerWidgetView: View {
    let entry: KickTrackerEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        switch family {
        case .accessoryRectangular, .accessoryCircular, .accessoryInline:
            lockScreenBody
        default:
            homeScreenBody
        }
    }

    private var homeScreenBody: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Шевеление")
                .font(.headline)
            Button(intent: RecordKickIntent()) {
                Text("Записать")
                    .frame(maxWidth: .infinity)
            }
            if let lastMoment = entry.lastMoment {
                Text("Последняя: \(format(timestampMillis: lastMoment.timestampMillis))")
                    .font(.caption)
            } else {
                Text("Пока нет записей")
                    .font(.caption)
            }
        }
        .padding()
        .containerBackground(.background, for: .widget)
    }

    private var lockScreenBody: some View {
        VStack(spacing: 4) {
            Text("Шевеление")
                .font(.caption)
            Button(intent: RecordKickIntent()) {
                Text("Записать")
            }
        }
        .containerBackground(.background, for: .widget)
    }

    private func format(timestampMillis: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestampMillis) / 1000)
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ru_RU")
        formatter.dateFormat = "d MMMM, HH:mm"
        return formatter.string(from: date)
    }
}

struct KickTrackerWidget: Widget {
    let kind = "KickTrackerWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: KickTrackerProvider()) { entry in
            KickTrackerWidgetView(entry: entry)
        }
        .configurationDisplayName("Шевеления")
        .description("Быстрая запись шевеления ребёнка.")
        .supportedFamilies([
            .systemSmall,
            .accessoryRectangular,
            .accessoryCircular,
            .accessoryInline,
        ])
    }
}

@main
struct KickTrackerWidgetBundle: WidgetBundle {
    var body: some Widget {
        KickTrackerWidget()
    }
}
