/*
 * SPDX-FileCopyrightText: Copyright (c) 2023-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
