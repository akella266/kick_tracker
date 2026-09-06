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
        VStack(alignment: .leading, spacing: 10) {
            Label("Шевеление", systemImage: "figure.child")
                .font(.subheadline.weight(.semibold))
                .lineLimit(1)

            Button(intent: RecordKickIntent()) {
                Label("Записать", systemImage: "plus.circle.fill")
                    .font(.subheadline.weight(.semibold))
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .buttonBorderShape(.roundedRectangle(radius: 12))

            Spacer(minLength: 0)

            if let lastMoment = entry.lastMoment {
                VStack(alignment: .leading, spacing: 1) {
                    Text("Последняя запись")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                    Text(formatDate(timestampMillis: lastMoment.timestampMillis))
                        .font(.caption.weight(.medium))
                        .lineLimit(1)
                        .minimumScaleFactor(0.75)
                    Text(formatTime(timestampMillis: lastMoment.timestampMillis))
                        .font(.caption.monospacedDigit().weight(.semibold))
                        .lineLimit(1)
                }
            } else {
                Text("Пока нет записей")
                    .font(.caption.weight(.medium))
                    .lineLimit(2)
            }
        }
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

    private func formatDate(timestampMillis: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestampMillis) / 1000)
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ru_RU")
        formatter.dateFormat = "d MMMM"
        return formatter.string(from: date)
    }

    private func formatTime(timestampMillis: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestampMillis) / 1000)
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ru_RU")
        formatter.dateFormat = "HH:mm:ss"
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
