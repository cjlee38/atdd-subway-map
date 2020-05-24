import {optionTemplate, subwayLinesItemTemplate} from "../../utils/templates.js";
import tns from "../../lib/slider/tiny-slider.js";
import {EVENT_TYPE} from "../../utils/constants.js";
import Modal from "../../ui/Modal.js";
import api from "../../api/index.js"

function AdminEdge() {
    const $subwayLinesSlider = document.querySelector(".subway-lines-slider");
    const $createEdgeButton = document.querySelector("#submit-button");
    const $selectedLine = document.querySelector("#station-select-options");
    const $preStationNameInput = document.querySelector("#depart-station-name");
    const $stationNameInput = document.querySelector("#arrival-station-name");
    let stations = [];

    const createSubwayEdgeModal = new Modal();

    async function initSubwayLinesSlider() {
        $subwayLinesSlider.innerHTML = "";
        let subwayLineInfos = await api.edge.get()
            .then(data => data);
        stations = await api.station.get();

        $subwayLinesSlider.innerHTML = subwayLineInfos
            .map(subwayLineInfo => subwayLinesItemTemplate(subwayLineInfo))
            .join("");

        tns({
            container: ".subway-lines-slider",
            loop: true,
            slideBy: "page",
            speed: 400,
            autoplayButtonOutput: false,
            mouseDrag: true,
            lazyload: true,
            controlsContainer: "#slider-controls",
            items: 1,
            edgePadding: 25
        });
    }

    async function initSubwayLineOptions() {
        let subwayLineInfos = await api.edge.get()
            .then(data => data);

        const subwayLineOptionTemplate = subwayLineInfos
            .map(subwayLineInfo => optionTemplate(subwayLineInfo))
            .join("");
        const $stationSelectOptions = document.querySelector(
            "#station-select-options"
        );
        $stationSelectOptions.insertAdjacentHTML(
            "afterbegin",
            subwayLineOptionTemplate
        );
    }

    const onCreateEdgeHandler = async event => {
        event.preventDefault();
        const selectedIndex = $selectedLine.selectedIndex;
        const lineId = $selectedLine.options[selectedIndex].getAttribute("data-line-id");
        const preStationName = $preStationNameInput.value;
        const stationName = $stationNameInput.value;
        let preStationId = null;
        let stationId = null;

        for (let i = 0; i < stations.length; i++) {
            if (stations[i].name === preStationName) {
                preStationId = stations[i].id;
            }
            if (stations[i].name === stationName) {
                stationId = stations[i].id;
            }
        }

        if (preStationId === stationId) {
            alert("서로 다른 역을 입력해주세요.");
        }

        const newEdge = {
            preStationId: preStationId,
            stationId: stationId,
            distance: 0,
            duration: 0
        };

        await api.edge.post(newEdge, lineId);
        createSubwayEdgeModal.toggle();
        initSubwayLinesSlider();
    };

    const onRemoveStationHandler = event => {
        const $target = event.target;
        const isDeleteButton = $target.classList.contains("mdi-delete");

        if (isDeleteButton) {
            const selectedLineId = $target.closest(".line").getAttribute("data-line-id");
            const selectedStationId = $target.closest(".list-item").getAttribute("data-id");
            api.edge.delete(selectedLineId, selectedStationId);
            $target.closest(".list-item").remove();
        }
        initSubwayLinesSlider();
    };

    const initEventListeners = () => {
        $subwayLinesSlider.addEventListener(EVENT_TYPE.CLICK, onRemoveStationHandler);
        $createEdgeButton.addEventListener(EVENT_TYPE.CLICK, onCreateEdgeHandler);
    };

    this.init = () => {
        initSubwayLinesSlider();
        initSubwayLineOptions();
        initEventListeners();
    };
}

const adminEdge = new AdminEdge();
adminEdge.init();
