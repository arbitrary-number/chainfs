/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (c) Arbitrary Number Project Team. All rights reserved.
 */
package com.github.chainfs.v4.test;

import com.github.chainfs.v4.UnlimitedScaleMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class UnlimitedScaleMapTest {

	@Test
	public void test() {
		UnlimitedScaleMap map = new UnlimitedScaleMap();

		map.put("testKey", "testValue", false);

		String result = map.get("testKey");
		Assertions.assertEquals("testValue", result);
	}
}
