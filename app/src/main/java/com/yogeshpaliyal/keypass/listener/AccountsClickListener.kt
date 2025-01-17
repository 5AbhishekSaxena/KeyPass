package com.yogeshpaliyal.keypass.listener

import android.view.View

/*
* @author Yogesh Paliyal
* techpaliyal@gmail.com
* https://techpaliyal.com
* created on 31-01-2021 09:00
*/
interface AccountsClickListener<T> {
    fun onItemClick(view: View, model: T)
    fun onCopyClicked(model: T)
}
