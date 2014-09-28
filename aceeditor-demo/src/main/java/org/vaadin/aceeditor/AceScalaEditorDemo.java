package org.vaadin.aceeditor;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import de.digitalculture.scalaeval.ScalaScriptEngine;
import org.vaadin.aceeditor.AceEditor.SelectionChangeEvent;
import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
import org.vaadin.aceeditor.client.AceAnnotation;
import org.vaadin.aceeditor.client.AceMarker;
import org.vaadin.aceeditor.client.AceMarker.OnTextChange;

import javax.script.ScriptEngine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;


@Theme("reindeer")
@StyleSheet("ace-markers.css")
@SuppressWarnings("serial")
@PreserveOnRefresh
public class AceScalaEditorDemo extends UI {

  //private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
  private ScriptEngine scriptEngine = new ScalaScriptEngine();
  private AceEditor editor = new AceEditor();

	private long latestMarkerId = 0L;
	private String newMarkerId() {
		return "m"+(++latestMarkerId);
	}

	private NativeSelect markerAnnotationSelect = new NativeSelect("Marker");
	{
		markerAnnotationSelect.setNullSelectionAllowed(false);
	}

	public AceScalaEditorDemo() {
		String s = "val l = List(1,2,3,4,5)\n"
				+ "l.filter(_ % 2 == 0)\n";

		editor.setValue(s);

		editor.focus();

	}

	@Override
	protected void init(VaadinRequest request) {

		final HorizontalSplitPanel split = new HorizontalSplitPanel();
		split.setSizeFull();
		setContent(split);
		split.setSplitPosition(35f);

		final VerticalLayout leftBar = new VerticalLayout();
		leftBar.setMargin(true);

		split.setFirstComponent(leftBar);

		editor.setSizeFull();

		// The Ace files are at webapp/static/ace directory.

		// The path depends on the server config.

//		editor.setThemePath("/static/ace");
//		editor.setModePath("/static/ace");
//		editor.setWorkerPath("/static/ace");

		// http://stackoverflow.com/a/3722122
//		editor.setThemePath("/aceeditor/static/ace");
//		editor.setModePath("/aceeditor/static/ace");
//		editor.setWorkerPath("/aceeditor/static/ace");




		leftBar.addComponent(createValueTextArea());

		//leftBar.addComponent(createCursorPanel());
		//leftBar.addComponent(createFocusPanel());

		//leftBar.addComponent(createThemeModePanel());
		//leftBar.addComponent(createOptionsPanel());

    /*
		leftBar.addComponent(createScrollPanel());

		leftBar.addComponent(createMarkerPanel());
		leftBar.addComponent(createRowAnnotationPanel());


		leftBar.addComponent(createMarkerAnnotationPanel());
		leftBar.addComponent(createErrorCheckingDemo());
		leftBar.addComponent(createSuggestionDemo());
		leftBar.addComponent(createErrorCorrectionDemo());
		*/

		VerticalLayout linkLayout = new VerticalLayout();
		linkLayout.addComponent(new Link("Addon at Vaadin Directory", new ExternalResource("http://vaadin.com/addon/aceeditor")));
		linkLayout.addComponent(new Link("Source at Github", new ExternalResource("https://github.com/ahn/vaadin-aceeditor/")));
		leftBar.addComponent(new Panel("Links", linkLayout));

		split.setSecondComponent(editor);

	}

	private Component createCursorPanel() {
		HorizontalLayout la = new HorizontalLayout();

		final TextField cursor = new TextField();
		cursor.setValue("0");

		Button bu = new Button("Set cursor");
		bu.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					int v = Integer.valueOf(cursor.getValue());
					editor.setCursorPosition(v);
				}
				catch (NumberFormatException e) {
					return;
				}
			}
		});

		editor.addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent e) {
				cursor.setValue(""+editor.getCursorPosition());

			}
		});

		la.addComponent(cursor);
		la.addComponent(bu);

		return new Panel("Cursor", la);
	}

	private Component createFocusPanel() {
		HorizontalLayout la = new HorizontalLayout();

		final Label label = new Label();

		editor.addFocusListener(new FocusListener() {
			@Override
			public void focus(FocusEvent event) {
				label.setValue("Editor focused");
			}
		});

		editor.addBlurListener(new BlurListener() {
			@Override
			public void blur(BlurEvent event) {
				label.setValue("Editor not focused");
			}
		});

		Button bu = new Button("Focus");
		bu.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				editor.focus();
			}
		});

		la.addComponent(bu);
		la.addComponent(label);
		return new Panel("Focus", la);
	}

	private Component createOptionsPanel() {
		VerticalLayout la = new VerticalLayout();
		final CheckBox readOnly = new CheckBox("Read-only");
		readOnly.setImmediate(true);
		readOnly.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setReadOnly(readOnly.getValue());
			}
		});

		final CheckBox wordwrap = new CheckBox("Word wrap");
		wordwrap.setImmediate(true);
		wordwrap.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setWordWrap(wordwrap.getValue());
			}
		});

		la.addComponent(readOnly);
		la.addComponent(wordwrap);

		return new Panel("Settings", la);
	}

	private Component createScrollPanel() {
		VerticalLayout ve = new VerticalLayout();
		HorizontalLayout la1 = new HorizontalLayout();
		final TextField row = new TextField(null, "0");
		Button b = new Button("Scroll to row");
		b.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					int i = Integer.valueOf(row.getValue());
					editor.scrollToRow(i);
				}
				catch (NumberFormatException e) {
					Notification.show("Give an integer.");
				}
			}
		});
		la1.addComponent(row);
		la1.addComponent(b);
		ve.addComponent(la1);

		HorizontalLayout la2 = new HorizontalLayout();
		final TextField pos = new TextField(null, "0");
		Button b2 = new Button("Scroll to position");
		b2.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					int i = Integer.valueOf(pos.getValue());
					editor.scrollToPosition(i);
				}
				catch (NumberFormatException e) {
					Notification.show("Give an integer.");
				}
			}
		});
		la2.addComponent(pos);
		la2.addComponent(b2);
		ve.addComponent(la2);

		return new Panel("Scroll to", ve);
	}

	private Component createValueTextArea() {

		final TextArea ta = new TextArea();
		ta.setWidth("100%");
		Button get = new Button("<<< getValue");
		Button set = new Button("setValue >>>");
		Button execute = new Button("Execute") {{

    }};

		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(ta);

		HorizontalLayout ho = new HorizontalLayout();
		layout.addComponent(ho);

		//ho.addComponent(get);
		//ho.addComponent(set);
		ho.addComponent(execute);

		get.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ta.setValue(editor.getValue());
			}
		});

		set.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				editor.setValue(ta.getValue());
			}
		});

    execute.addClickListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        try {
          Object result = scriptEngine.eval(editor.getValue());
          ta.setValue(result.toString());
        } catch (Throwable e) {
          e.printStackTrace();
          ByteArrayOutputStream bout = new ByteArrayOutputStream();
          e.printStackTrace(new PrintStream(bout));
          try {
            bout.close();
          } catch (IOException ignored) {
          }

          ta.setValue(new String(bout.toByteArray()));
        }
      }
    });

		return new Panel("Value", layout);
	}

	private Component createMarkerPanel() {

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);

		final HorizontalLayout markerLayout = new HorizontalLayout();

		HorizontalLayout ho = new HorizontalLayout();
		final TextField cssField = new TextField("CSS Class", "mymarker1");
		ho.addComponent(cssField);
		final NativeSelect typeSelect = new NativeSelect("Type");
		ho.addComponent(typeSelect);
		for (AceMarker.Type item : AceMarker.Type.values()) {
			typeSelect.addItem(item);
		}
		typeSelect.select(AceMarker.Type.line);
		typeSelect.setNullSelectionAllowed(false);
		final CheckBox inFrontCheck = new CheckBox("InFront");
		ho.addComponent(inFrontCheck);
		ho.setComponentAlignment(inFrontCheck, Alignment.BOTTOM_LEFT);

		final NativeSelect changeSelect = new NativeSelect("OnTextChange");
		ho.addComponent(changeSelect);
		for (OnTextChange item : OnTextChange.values()) {
			changeSelect.addItem(item);
		}
		changeSelect.select(OnTextChange.DEFAULT);
		changeSelect.setNullSelectionAllowed(false);

		layout.addComponent(ho);

		Button button = new Button("Add marker to selection");
		layout.addComponent(button);
		button.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				TextRange selection = editor.getSelection();
				if (selection.isZeroLength()) {
					Notification.show("Select some text first");
					return;
				}
				String css = cssField.getValue();
				AceMarker.Type type = (AceMarker.Type)typeSelect.getValue();
				boolean inFront = inFrontCheck.getValue();
				OnTextChange onChange = (OnTextChange) changeSelect.getValue();
				final String markerId = newMarkerId();
				AceMarker m = new AceMarker(markerId, selection, css, type, inFront, onChange);
				editor.addMarker(m);
				final Button mb = new Button(""+markerId);
				markerLayout.addComponent(mb);
				markerAnnotationSelect.addItem(markerId);
				mb.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						editor.removeMarker(markerId);
						markerLayout.removeComponent(mb);
						markerAnnotationSelect.removeItem(markerId);
					}
				});
				
			}
		});
		
		
		layout.addComponent(markerLayout);
		
		return new Panel("Markers", layout);
	}
	

	
	private Component createRowAnnotationPanel() {
		
		VerticalLayout layout = new VerticalLayout();
		
		HorizontalLayout ho = new HorizontalLayout();
		
		final TextField msgField = new TextField("Message","Hello");
		ho.addComponent(msgField);
		
		final NativeSelect typeSelect = new NativeSelect("Type");
		typeSelect.setNullSelectionAllowed(false);
		for (AceAnnotation.Type type : AceAnnotation.Type.values()) {
			typeSelect.addItem(type);
		}
		typeSelect.select(AceAnnotation.Type.info);
		
		ho.addComponent(typeSelect);
		
		final TextField rowField = new TextField("Row","2");
		ho.addComponent(rowField);
		
		Button add = new Button("Add");
		add.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				AceAnnotation.Type type = (AceAnnotation.Type)typeSelect.getValue();
				int row = 0;
				try {
					row = Integer.valueOf(rowField.getValue());
				}
				catch (NumberFormatException e) {
					Notification.show(rowField.getValue() + " is not a number");
					return;
				}
				String msg = msgField.getValue();
				editor.addRowAnnotation(new AceAnnotation(msg, type), row);
			}
		});
		
		Button clear = new Button("Clear");
		clear.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				editor.clearRowAnnotations();
			}
		});
		
		layout.addComponent(ho);
		layout.addComponent(add);
		layout.addComponent(clear);
		
		return new Panel("Row Annotations", layout);
	}
	
	private Component createMarkerAnnotationPanel() {
		
		VerticalLayout layout = new VerticalLayout();
		
		HorizontalLayout ho = new HorizontalLayout();
		
		final TextField msgField = new TextField("Message","Hello");
		ho.addComponent(msgField);
		
		final NativeSelect typeSelect = new NativeSelect("Type");
		typeSelect.setNullSelectionAllowed(false);
		for (AceAnnotation.Type type : AceAnnotation.Type.values()) {
			typeSelect.addItem(type);
		}
		typeSelect.select(AceAnnotation.Type.info);
		
		ho.addComponent(typeSelect);
		
		ho.addComponent(markerAnnotationSelect );
		
		Button add = new Button("Add");
		add.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				AceAnnotation.Type type = (AceAnnotation.Type)typeSelect.getValue();
				String msg = msgField.getValue();
				String markerId = (String) markerAnnotationSelect.getValue();
				if (markerId==null) {
					Notification.show("Select marker");
					return;
				}
				editor.addMarkerAnnotation(new AceAnnotation(msg, type), markerId);
			}
		});
		
		Button clear = new Button("Clear");
		clear.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				editor.clearRowAnnotations();
			}
		});
		
		layout.addComponent(ho);
		layout.addComponent(add);
		layout.addComponent(clear);
		
		return new Panel("Marker Annotations", layout);
	}

	private Component createThemeModePanel() {
		
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		
		HorizontalLayout ve = new HorizontalLayout();
		
		final NativeSelect themeSelect = new NativeSelect("Theme");
		ve.addComponent(themeSelect);
		for (AceTheme theme : AceTheme.values()) {
			themeSelect.addItem(theme);
		}
		themeSelect.setNullSelectionAllowed(false);
		themeSelect.setImmediate(true);
		themeSelect.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setTheme((AceTheme)themeSelect.getValue());
			}
		});
		themeSelect.select(AceTheme.eclipse);
		
		final NativeSelect modeSelect = new NativeSelect("Mode");
		ve.addComponent(modeSelect);
		for (AceMode mode : AceMode.values()) {
			modeSelect.addItem(mode);
		}
		modeSelect.setNullSelectionAllowed(false);
		modeSelect.setImmediate(true);
		modeSelect.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setMode((AceMode)modeSelect.getValue());
			}
		});
		modeSelect.select(AceMode.scala);
		
		layout.addComponent(ve);
		
		final CheckBox cb = new CheckBox("useWorker");
		cb.setImmediate(true);
		cb.setValue(true);
		cb.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setUseWorker(cb.getValue());
			}
		});
		
		ve.addComponent(cb);
		ve.setComponentAlignment(cb, Alignment.BOTTOM_LEFT);
		
		return new Panel("Theme & Mode", layout);
	}
	
	private Component createErrorCheckingDemo() {
		Button b = new Button("Open error-checker demo");
		b.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				Window w = new Window("Don't write X's");
				w.setWidth("50%");
				w.setHeight("50%");
				w.center();
				AceEditor ee = new AceEditor();
				ee.setValue("Don't write X's!");
				ee.setSizeFull();
				w.setContent(ee);
				UI.getCurrent().addWindow(w);
				
				MyErrorChecker checker = new MyErrorChecker();
				checker.attachTo(ee);
			}
			
		});
		
		return new Panel("Error Checker", b);
	}
	
	private Component createSuggestionDemo() {
		Button b = new Button("Open suggestion demo");
		b.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				Window w = new Window("Press Ctrl+Space for suggestions.");
				w.setWidth("50%");
				w.setHeight("50%");
				w.center();
				AceEditor ee = new AceEditor();
				ee.setValue("Press Ctrl+Space for suggestions.\nOr type a dot.");
				ee.setSizeFull();
				w.setContent(ee);
				UI.getCurrent().addWindow(w);
				
				new SuggestionExtension(new MySuggester()).extend(ee);
			}
			
		});
		
		return new Panel("Suggestions", b);
	}

	private Component createErrorCorrectionDemo() {
		Button b = new Button("Open auto-correction demo");
		b.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				Window w = new Window("Leet-speakerizer");
				w.setWidth("50%");
				w.setHeight("50%");
				w.center();
				AceEditor ee = new AceEditor();
				ee.setTextChangeTimeout(200);
				ee.setWordWrap(true);
				ee.setValue("The text you type is auto-corrected to leet-speak on the server-side.\n\n" +
						"What's nice is that none of the stuff you type gets lost, " +
						"even when typed while visiting the server.");
				ee.setSizeFull();
				w.setContent(ee);
				UI.getCurrent().addWindow(w);
				
				LeetSpeakerizer leeter = new LeetSpeakerizer();
				leeter.attachTo(ee);
			}
			
		});
		
		return new Panel("Auto-correction", b);
	}

}
