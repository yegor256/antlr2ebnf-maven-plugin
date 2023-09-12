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