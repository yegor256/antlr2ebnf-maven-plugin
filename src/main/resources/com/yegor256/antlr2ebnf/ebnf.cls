% The MIT License (MIT)
%
% Copyright (c) 2023-2025 Yegor Bugayenko
%
% Permission is hereby granted, free of charge, to any person obtaining a copy
% of this software and associated documentation files (the "Software"), to deal
% in the Software without restriction, including without limitation the rights
% to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
% copies of the Software, and to permit persons to whom the Software is
% furnished to do so, subject to the following conditions:
%
% The above copyright notice and this permission notice shall be included
% in all copies or substantial portions of the Software.
%
% THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
% IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
% FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
% AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
% LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
% OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
% SOFTWARE.

\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{enbf}
\LoadClass[12pt]{article}
\RequirePackage[T1]{fontenc}
\RequirePackage{lmodern}
\RequirePackage[usenames,dvipsnames]{xcolor}
\RequirePackage{microtype}
\RequirePackage{naive-ebnf}

\renewcommand\EbnfSpecial[1]{\textcolor{MidnightBlue}{\texttt{#1}}}
\renewcommand\EbnfTerminal[1]{\textcolor{Orange}{\texttt{"#1"}}}
\renewcommand\EbnfRegex[1]{\textcolor{PineGreen}{\texttt{#1}}}

\AtBeginDocument{%
    \pagestyle{empty}%
    \noindent%
}

\endinput
