/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023-2024 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

grammar Program;

program: alpha | beta;

alpha: BOOL | NAME;

beta: 'test';

BOOL: 'TRUE' | 'FALSE';

NAME: [a-z] ~[ \r\n\t,.|':;!?\][}{)(]*;

F01: 'test1';
F02: 'test2';
F03: 'test3';
F04: 'test4';
F05: 'test5';
F06: 'test6';
F07: 'test7';
F08: 'test8';
F09: 'test9';
F10: 'test10';
F11: 'test11';
F12: 'test12';
F13: 'test13';
F14: 'test14';
F15: 'test15';
F16: 'test16';
F17: 'test17';
F18: 'test18';
F19: 'test19';
F20: 'test20';
F21: 'test21';
F22: 'test22';
F23: 'test23';
F24: 'test24';
F25: 'test25';
F26: 'test26';
F27: 'test27';
F28: 'test28';
F29: 'test29';
F30: 'test30';
F31: 'test31';
F32: 'test32';
F33: 'test33';
