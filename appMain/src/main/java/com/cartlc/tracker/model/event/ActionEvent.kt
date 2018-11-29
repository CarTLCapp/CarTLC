package com.cartlc.tracker.model.event

import com.cartlc.tracker.model.flow.Action

class ActionEvent(value: Action) : LiveEvent<Action>(value)