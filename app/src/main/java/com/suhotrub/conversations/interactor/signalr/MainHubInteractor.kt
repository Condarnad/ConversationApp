package com.suhotrub.conversations.interactor.signalr

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.smartarmenia.dotnetcoresignalrclientjava.HubConnection
import com.smartarmenia.dotnetcoresignalrclientjava.HubConnectionListener
import com.smartarmenia.dotnetcoresignalrclientjava.HubEventListener
import com.smartarmenia.dotnetcoresignalrclientjava.HubMessage
import com.suhotrub.conversations.base.App
import com.suhotrub.conversations.interactor.user.TokenStorage
import com.suhotrub.conversations.ui.util.subscribe
import com.suhotrub.conversations.ui.util.ui.showError
import com.suhotrub.conversations.ui.util.ui.showInfiniteError
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class MainHubInteractor @Inject constructor(
        private val tokenStorage: TokenStorage,
        private val context: Context,
        @Named("BASE_URL") private val baseUrl: String
) {
    init {
        createHubConnection()
    }

    private var onHubConnected: BehaviorSubject<HubConnection>? = null

    private val listeners = hashMapOf<String, PublishSubject<HubMessage>>()

    private lateinit var hubConnection: HubConnection
    fun createHubConnection() {

        if (::hubConnection.isInitialized) {
            try {
                hubConnection.disconnect()
            } catch (t: Throwable) {
            }
        }

        hubConnection = ResponseHubConnection("${baseUrl}signalr?access_token=${tokenStorage.getToken()}", "Bearer ${tokenStorage.getToken()}")

        onHubConnected = BehaviorSubject.create()

        var shown = false
        hubConnection.addListener(object : HubConnectionListener {
            override fun onConnected() {
                Thread {
                    Thread.sleep(1000)
                    onHubConnected?.onNext(hubConnection)
                    onHubConnected?.onComplete()
                }
                onHubConnected = null

                shown = false
                (context as? App)?.currentActivity?.showError("Подключено")

                Timber.d("Hub: Connected")

            }

            override fun onMessage(message: HubMessage?) {
                Timber.d("Hub: ${message?.toString() ?: ""}")
                try {
                    if (message != null)
                        listeners[message.target]?.onNext(message)
                } catch (t: Throwable) {
                }
            }

            override fun onDisconnected() {
                subscribe(Observable.just(1).delay(2, TimeUnit.SECONDS)) {
                    hubConnection.connect()
                }

                if (!shown)
                    (context as? App)?.currentActivity?.showInfiniteError("Подключаемся к хабу")
                shown = true

                Timber.d("Hub: Disconnected")
            }

            override fun onError(exception: Exception?) {
                Log.d("HubConnection", exception?.message, exception)

                try {
                    if (!hubConnection.isConnected) {
                        onDisconnected()
                    }
                } catch (t: Throwable) {
                }
            }
        })
        hubConnection.connect()

    }

    fun safeOperation(action: (HubConnection) -> Unit) {
        try {
            if (onHubConnected != null) {
                onHubConnected?.doOnNext {
                    try {
                        if (::hubConnection.isInitialized && hubConnection.isConnected)
                            action(hubConnection)
                    } catch (t: Throwable) {
                    }
                }?.subscribe()
            } else if (onHubConnected == null && ::hubConnection.isInitialized && hubConnection.isConnected) {
                try {
                    action(hubConnection)
                } catch (t: Throwable) {
                }
            } else {

                createHubConnection()

                onHubConnected?.doOnComplete {
                    if (::hubConnection.isInitialized && hubConnection.isConnected)
                        try {
                            action(hubConnection)
                        } catch (t: Throwable) {
                        }
                }?.subscribe()
            }
        } catch (t: Throwable) {
        }
    }


    fun stopHubConnection() = {}

    fun <T> observeEvent(eventName: String, type: Class<T>): Observable<T> {
        val publisher = listeners.getOrPut(eventName) {
            PublishSubject.create()
        }
        val gson = Gson()

        return publisher.map { gson.fromJson<T>(it.arguments[0], type) }
    }
}

inline fun <reified T : Any> HubConnection.invokeEvent(eventName: String, vararg params: Any): Observable<T> {
    val kekEventName =
            if (this is ResponseHubConnection) {
                this.invokeCallback(eventName, *params.asList().toTypedArray())
            } else {
                this.invoke(eventName, *params.asList().toTypedArray())
                eventName
            }
    return observeEventOnce(kekEventName)

}

inline fun <reified T : Any> HubConnection.observeEventOnce(eventName: String): Observable<T> {
    val observable = BehaviorSubject.create<T>()
    val gson = Gson()
    val listener = HubEventListener {
        try {
            gson.fromJson<T>(it.target.split("result\":")[1].dropLast(1), T::class.java).let {
                observable.onNext(it)
            }
        }catch (t:Throwable){
            observable.onError(t)
        }
    }
    observable.doOnNext {
        unSubscribeFromEvent(eventName, listener)
    }.doOnError {
        unSubscribeFromEvent(eventName, listener)
    }
    subscribeToEvent(eventName, listener)
    return observable
}