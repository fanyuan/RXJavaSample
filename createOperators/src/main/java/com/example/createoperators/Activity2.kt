package com.example.createoperators

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class Activity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
    }

    fun create(view: View) {
        val observale:Observable<String> = Observable.create { observableEmitter ->
            Utils.log("subscribe --> ${Thread.currentThread().name}" )
            observableEmitter.onNext("hello kotlin")
            observableEmitter.onComplete()
        }
        observale.run {
            subscribeOn(Schedulers.io())
            observeOn(AndroidSchedulers.mainThread())
        }

        val observer = object : Observer<String>{
            override fun onSubscribe(d: Disposable) {
                Utils.log(
                    "onSubscribe --> ${Thread.currentThread().name} \n $d"
                )
            }

            override fun onNext(s: String) {
                Utils.log(
                    """onNext --> ${Thread.currentThread().name} 
$s"""
                )
            }

            override fun onError(e: Throwable) {
                Utils.log(
                    """onError --> ${Thread.currentThread().name} 
$e"""
                )
            }

            override fun onComplete() {
                Utils.log("onComplete --> ${Thread.currentThread().name}" )
            }
        }

        observale.subscribe (observer)
    }
}