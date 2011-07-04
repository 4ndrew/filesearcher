latexfile = ReadMe

LATEX = latex
PDFLATEX = pdflatex

$(latexfile).pdf :
	$(PDFLATEX) $(latexfile)

pdf: $(latexfile).pdf

clean:
	rm -f *.aux *.out *.log *.pdfsync
	
cleanall: clean
	rm -f $(latexfile).pdf