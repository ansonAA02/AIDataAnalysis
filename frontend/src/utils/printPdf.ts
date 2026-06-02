// PDF export helper: triggers the browser's native print dialog so the user
// can choose "Save as PDF". Pairs with the global @media print stylesheet
// in styles/print.css that hides chrome (sidebar, top bar, buttons) so only
// the report content gets printed.
//
// Why not jsPDF? It would add ~700KB to the bundle and produce inferior
// rendering compared to the browser's print engine. Native print + CSS gives
// us pixel-perfect output with zero new dependencies.

// Trigger the browser print dialog with a temporarily-set document title so
// the default PDF filename matches what we want.
export function printAsPdf(filenameHint: string): void {
  const previousTitle = document.title;
  // Browsers use document.title as the default Save-As-PDF filename.
  document.title = filenameHint;
  try {
    window.print();
  } finally {
    // Restore the original tab title even if the user cancels.
    setTimeout(() => {
      document.title = previousTitle;
    }, 0);
  }
}

// Build a print-friendly filename hint from a context label and period.
export function buildPrintFilename(context: string, periodLabel: string): string {
  return `${context}_${periodLabel}`;
}
