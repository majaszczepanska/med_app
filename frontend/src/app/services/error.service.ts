import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ErrorService {

  handleErrors(err: any): string {
    console.error(err);

    if (err.error && typeof err.error === 'string') {
      try {
        err.error = JSON.parse(err.error);
      } catch (e) {
      }
    }
    if (err.error && err.error.message) {
      //alert("❌ ERROR: " + err.error.message);
      return "❌ " + err.error.message;
    }
    if (err.error && typeof err.error === 'object') {
      //let errorMessage = "❌ VALIDATION ERRORS:\n";
      let errorMessage = '';
      let hasValidationErrors = false;

        for (const key in err.error) {
          if (err.error.hasOwnProperty(key)) {
            hasValidationErrors = true;
            //errorMessage += `\n• ${key.toUpperCase()}:\n`;
            const fullErrorText = err.error[key];
            let lines = fullErrorText.split(',');
            /*lines = lines
              .map((line: string) => line.trim())
              .sort((a: string, b: string) => b.localeCompare(a));
            lines.forEach((line: string) => {
              errorMessage += `    • ${line}\n`;
            });*/
            lines.forEach((line: string) => {
              errorMessage += `❌ ${line.trim()}\n`;
            });
          } 
        }
        if (hasValidationErrors) {
            //alert(errorMessage);
            return errorMessage.trim();
        }
    } 
    if (typeof err.error === 'string') {
      //alert("❌ ERROR: " + err.error);
      return "❌ " + err.error;
    } 
    //alert("❌ ERROR: " + (err.error || "Server error"));
    return "❌ Server error or invalid credentials.";
  }
}
