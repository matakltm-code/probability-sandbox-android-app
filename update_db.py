with open('app/src/main/java/com/example/data/local/AppDatabase.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '@Database(entities = [ActivityLog::class, ToolProfile::class, SiteRule::class], version = 3, exportSchema = false)\nabstract class AppDatabase : RoomDatabase() {\n    abstract fun activityLogDao(): ActivityLogDao\n    abstract fun toolProfileDao(): ToolProfileDao\n    abstract fun siteRuleDao(): SiteRuleDao',
    '@Database(entities = [ActivityLog::class, ToolProfile::class, SiteRule::class, Bookmark::class], version = 4, exportSchema = false)\nabstract class AppDatabase : RoomDatabase() {\n    abstract fun activityLogDao(): ActivityLogDao\n    abstract fun toolProfileDao(): ToolProfileDao\n    abstract fun siteRuleDao(): SiteRuleDao\n    abstract fun bookmarkDao(): BookmarkDao'
)

with open('app/src/main/java/com/example/data/local/AppDatabase.kt', 'w') as f:
    f.write(content)
