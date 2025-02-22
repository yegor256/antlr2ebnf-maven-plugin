/**
 * The MIT License (MIT)
 *
 * SPDX-FileCopyrightText: Copyright (c) 2023-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

[
  'target/ebnf/org/example/Program.txt',
  'target/ebnf/org/example/Program.pdf',
  'target/ebnf-latex/article.aux',
].each { assert new File(basedir, it).exists() }

true
