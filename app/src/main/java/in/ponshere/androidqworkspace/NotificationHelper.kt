/*
 * Copyright (C) 2019 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bubbles.data

import `in`.ponshere.androidqworkspace.BubbleActivity
import `in`.ponshere.androidqworkspace.MainActivity
import `in`.ponshere.androidqworkspace.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.WorkerThread
import androidx.core.graphics.applyCanvas

/**
 * Handles all operations related to [Notification].
 */
class NotificationHelper(private val context: Context) {

    companion object {
        /**
         * The notification channel for messages. This is used for showing Bubbles.
         */
        private const val CHANNEL_NEW_MESSAGES = "new_messages"

        private const val REQUEST_CONTENT = 1
        private const val REQUEST_BUBBLE = 2
    }

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun setUpNotificationChannels() {
        if (notificationManager.getNotificationChannel(CHANNEL_NEW_MESSAGES) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_NEW_MESSAGES,
                    CHANNEL_NEW_MESSAGES,
                    // The importance must be IMPORTANCE_HIGH to show Bubbles.
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "All new incoming messages"
                }
            )
        }
    }

    @WorkerThread
    fun showNotification(chat: String, fromUser: Boolean) {
        val icon = Icon.createWithResource(context,R.drawable.ic_message)
        val person = Person.Builder()
            .setName(chat)
            .setIcon(icon)
            .build()
        val contentUri = Uri.parse("https://android.example.com/chat/1")
        val builder = Notification.Builder(context, CHANNEL_NEW_MESSAGES)
            // A notification can be shown as a bubble by calling setBubbleMetadata()
            .setBubbleMetadata(
                Notification.BubbleMetadata.Builder()
                    // The height of the expanded bubble.
                    .setDesiredHeight(600)
                    // The icon of the bubble.
                    // TODO: The icon is not displayed in Android Q Beta 2.
                    .setIcon(icon)
                    .apply {
                        // When the bubble is explicitly opened by the user, we can show the bubble automatically
                        // in the expanded state. This works only when the app is in the foreground.
                        // TODO: This does not yet work in Android Q Beta 2.
                        if (fromUser) {
                            setAutoExpandBubble(true)
                            setSuppressInitialNotification(true)
                        }
                    }
                    // The Intent to be used for the expanded bubble.
                    .setIntent(
                        PendingIntent.getActivity(
                            context,
                            REQUEST_BUBBLE,
                            // Launch BubbleActivity as the expanded bubble.
                            Intent(context, BubbleActivity::class.java)
                                .setAction(Intent.ACTION_VIEW)
                                .setData(Uri.parse("https://android.example.com/chat/1")),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                    .build()
            )
            // The user can turn off the bubble in system settings. In that case, this notification is shown as a
            // normal notification instead of a bubble. Make sure that this notification works as a normal notification
            // as well.
            .setContentTitle(chat)
            .setSmallIcon(R.drawable.ic_message)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .addPerson(person)
            .setShowWhen(true)
            // The content Intent is used when the user clicks on the "Open Content" icon button on the expanded bubble,
            // as well as when the fall-back notification is clicked.
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    REQUEST_CONTENT,
                    Intent(context, BubbleActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(contentUri),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        builder.setContentText("Content")
        notificationManager.notify(1, builder.build())
    }

    fun dismissNotification(id: Long) {
        notificationManager.cancel(id.toInt())
    }

    fun canBubble(): Boolean {
        val channel = notificationManager.getNotificationChannel(CHANNEL_NEW_MESSAGES)
        return notificationManager.areBubblesAllowed() && channel.canBubble()
    }
}
