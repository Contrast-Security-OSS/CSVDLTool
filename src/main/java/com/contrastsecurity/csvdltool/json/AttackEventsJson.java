/*
 * MIT License
 * Copyright (c) 2020 Contrast Security Japan G.K.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package com.contrastsecurity.csvdltool.json;

import java.util.List;
import java.util.StringJoiner;

import com.contrastsecurity.csvdltool.model.AttackEvent;

public class AttackEventsJson extends ContrastJson {
    private List<AttackEvent> attackEvents;

    public List<AttackEvent> getAttackEvents() {
        return attackEvents;
    }

    public void setAttackEvents(List<AttackEvent> attackEvents) {
        this.attackEvents = attackEvents;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\r\n");
        for (AttackEvent events : this.attackEvents) {
            sj.add(events.toString());
        }
        return sj.toString();
    }

}
