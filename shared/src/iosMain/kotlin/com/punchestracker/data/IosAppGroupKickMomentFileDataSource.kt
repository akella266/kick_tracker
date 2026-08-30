package com.punchestracker.data

import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL

class IosAppGroupKickMomentFileDataSource(
    private val appGroupId: String = KickStorageConstants.APP_GROUP_ID,
    private val fileName: String = KickStorageConstants.FILE_NAME,
) : KickMomentFileDataSource {
    override suspend fun readText(): String? {
        val url = fileUrl() ?: return null
        val data = NSData.dataWithContentsOfURL(url) ?: return null
        return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }

    override suspend fun writeTextAtomically(text: String) {
        val url = fileUrl() ?: error("App Group container is unavailable: $appGroupId")
        val nsString = NSString.create(string = text)
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: error("Unable to encode kick moment JSON")
        val success = data.writeToURL(url, atomically = true)
        if (!success) error("Unable to write kick moment JSON")
    }

    private fun fileUrl(): NSURL? {
        val containerUrl = NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(appGroupId)
            ?: return null
        return containerUrl.URLByAppendingPathComponent(fileName)
    }
}
