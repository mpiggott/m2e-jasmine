package ca.piggott.m2e.jasmine.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

public class JasmineListener implements DisposeListener, LocationListener {
	private List<BrowserFunction> registeredFn;

	private final Browser browser;

	public JasmineListener(Browser browser) {
		registeredFn = new ArrayList<BrowserFunction>();
		this.browser = browser;
		browser.addDisposeListener(this);
		browser.addLocationListener(this);
		registerFunctions();
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		disposeFunctions();
	}

	@Override
	public void changing(LocationEvent event) {
		disposeFunctions();
	}

	@Override
	public void changed(LocationEvent event) {
		registerFunctions();
	}
	
	private synchronized void registerFunctions() {
		addFunction(new BrowserFunction(browser, ""));
	}

	private synchronized void addFunction(BrowserFunction function) {
		registeredFn.add(function);
	}
	
	private synchronized void disposeFunctions() {
		for (BrowserFunction fn : registeredFn) {
			fn.dispose();
		}
		registeredFn = new ArrayList<>();
	}
}
